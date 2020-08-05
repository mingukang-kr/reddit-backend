package com.reddit.service;

import static com.reddit.util.Constants.ACTIVATION_EMAIL;
import static java.time.Instant.now;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reddit.dto.AuthenticationResponse;
import com.reddit.dto.LoginRequest;
import com.reddit.dto.RefreshTokenRequest;
import com.reddit.dto.RegisterRequest;
import com.reddit.exception.SpringRedditException;
import com.reddit.model.NotificationEmail;
import com.reddit.model.User;
import com.reddit.model.User.Authority;
import com.reddit.model.VerificationToken;
import com.reddit.repository.UserRepository;
import com.reddit.repository.VerificationTokenRepository;
import com.reddit.security.CustomAuthenticationProvider;
import com.reddit.security.CustomUsernamePasswordAuthenticationFilter;
import com.reddit.security.JwtProvider;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	// 인증 메일 전송을 위한 클래스
	private final MailContentBuilder mailContentBuilder;
	private final MailService mailService;
	// 토큰 인증 방식 로그인을 위한 클래스
	private final VerificationTokenRepository verificationTokenRepository;
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;
	private final CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter;
	private final CustomAuthenticationProvider customAuthenticationProvider;
	
	@Transactional
	public void signup(RegisterRequest registerRequest) {

		// 1. 가입 신청한 회원의 정보를 DB에 저장 (메일 인증이 안 된 상태이므로 최종 가입이 된 것은 아님)
		User user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		user.setCreated(now());
		user.setEnabled(false); // 메일 인증을 통해 최종 회원 가입이 될 시에 true로 바꿔준다.
		userRepository.save(user);
		log.info("가입 신청 회원의 정보가 DB에 저장되었습니다.");

		// 2. 회원 가입 완료를 위해 가입 인증 메일을 보냄
		String token = generateVerificationToken(user);
		String message = mailContentBuilder.build("가입 확인 인증 메일입니다. 링크를 클릭하시면 회원 가입이 완료됩니다. : " 
				+ ACTIVATION_EMAIL + "/" + token); // 컨트롤러의 요청 매핑 주소에 토큰을 붙여서 메일을 보낸다.
		mailService.sendMail(new NotificationEmail("가입을 완료하시려면 클릭하세요.", user.getEmail(), message));
	}
	
	public AuthenticationResponse login(LoginRequest loginRequest) {
		
		/* 1. 직접 구현한 로그인 인증 필터로 입력한 아이디와 비밀번호가 들어간 토큰을 만든다. */
		Authentication needToBeAuthenticated = customUsernamePasswordAuthenticationFilter.customAttemptAuthentication(loginRequest);
		
		/* 2. 직접 구현한 AuthenticationProvider에서 인증을 통과하면 Authentication을 구현한 객체를 반환한다.
		 * AuthenticationManager -> (구현) ProviderManager -> 해당 토큰을 인증할 AuthencationProvider를 찾음 */
		Authentication authenticated = customAuthenticationProvider.authenticate(needToBeAuthenticated);
		
		// 3. 인증 완료된 토큰을 Context -> ContextHolder에 저장한다.
		SecurityContextHolder.getContext().setAuthentication(authenticated);
		
		// 4. keyStore에 만들어둔 인증서로 인증 정보(Authentication)에 서명하여 인증 토큰을 만든다.
		String token = jwtProvider.generateToken(authenticated);

		// 5. 직접 만든 인증 응답 객체에 각종 데이터(인증 토큰, Refresh 토큰 등..)를 바인딩한 뒤 반환한다.
		return AuthenticationResponse.builder()
				.authenticationToken(token)
				.refreshToken(refreshTokenService.generateRefreshToken().getToken())
				.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
				.username(loginRequest.getUsername())
				.build();
	}
	
	@Transactional(readOnly = true)
	public User getCurrentUser() {
		
		org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User)SecurityContextHolder.
				getContext().getAuthentication().getPrincipal();
		
		return userRepository.findByUsername(principal.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException(principal.getUsername() + "라는 사용자를 찾을 수 없습니다."));
	}

	// 인증 토큰 생성 메소드
	private String generateVerificationToken(User user) {

		String token = UUID.randomUUID().toString();

		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setUser(user);

		verificationTokenRepository.save(verificationToken);

		return token;
	}

	// 가입 확인 메일을 눌러서 돌아온 토큰이 유효한 토큰인지 검사하는 메소드
	public void verifyAccount(String token) {

		Optional<VerificationToken> verificationTokenOptional = verificationTokenRepository.findByToken(token);
		verificationTokenOptional
			.orElseThrow(() -> new SpringRedditException("인증 정보가 올바르지 않습니다."));
		
		fetchUserAndEnable(verificationTokenOptional.get());
	}

	// 인증된 토큰으로부터 사용자 정보를 DB에서 불러오는 메소드
	@Transactional
	private void fetchUserAndEnable(VerificationToken verificationToken) {

		User user = verificationToken.getUser();
		user.setEnabled(true); // 가입 인증을 완료하면 true로 바꿔준다.
		user.setAuthority(Authority.ROLE_USER); // 처음 가입시 권한은 'ROLE_USER'이다.

		userRepository.save(user); // 다시 새로 회원정보가 처음부터 저장되는 것이 아니라, enabled만 바뀌어서 DB에도 적용된다.
		log.info("회원 가입이 최종적으로 완료되었습니다.");
	}
	
    public boolean isLoggedIn() {
    	
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }
    
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
    	
    	refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
    	String token = jwtProvider.generateTokenWithUserName(refreshTokenRequest.getUsername());
    	
    	return AuthenticationResponse.builder()
    			.authenticationToken(token)
    			.refreshToken(refreshTokenRequest.getRefreshToken())
    			.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
    			.username(refreshTokenRequest.getUsername())
    			.build();
    }
}
