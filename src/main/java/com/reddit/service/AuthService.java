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
import com.reddit.security.jwt.CustomAccessTokenProvider;
import com.reddit.security.jwt.CustomRefreshTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	// 인증 메일 전송을 위한 클래스
	private final MailContentBuilder mailContentBuilder;
	private final MailService mailService;
	// 토큰 인증 방식 로그인을 위한 클래스
	private final VerificationTokenRepository verificationTokenRepository;
	private final CustomAccessTokenProvider jwtProvider;
	private final CustomRefreshTokenProvider refreshTokenService;
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
	/* 1. 스프링 시큐리티 */
		/* 1. 로그인 인증 필터로 입력한 아이디와 비밀번호가 들어간 토큰을 만든다. */
		Authentication needToBeAuthenticated = customUsernamePasswordAuthenticationFilter.AttemptAuthentication(loginRequest);
		
		/* 2. AuthenticationProvider에서 인증을 진행하고, 인증이 완료되면 Authentication 객체를 반환한다.
		 * ProviderManager -> 해당 토큰을 인증할 AuthencationProvider를 찾는다.
		 * 직접 구현한 AuthenticationProvider에서 인증을 진행한다.
		 * */
		Authentication authenticated = customAuthenticationProvider.authenticate(needToBeAuthenticated);
		
		// 3. 인증 완료된 Authentication 객체를 Security 컨텍스트에 저장한다.
		SecurityContextHolder.getContext().setAuthentication(authenticated);
		
	/* 2. JWT를 이용한 인증 시스템 */
		// 1. Authentication 객체에 인증서로 서명하여 인증 토큰(엑세스 토큰)을 만든다.
		String accessToken = jwtProvider.generateToken(authenticated);

		// 2. 응답 객체에 각종 데이터(Access 토큰, Refresh 토큰 등)를 넣은 뒤 반환한다.
		return AuthenticationResponse.builder()
				.authenticationToken(accessToken)
				.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
				.refreshToken(refreshTokenService.generateRefreshToken().getToken())
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
    	/*
    	 * 만료된 엑세스 토큰도 기간은 만료되었지만 조작되지는 않았는지 검사해야한다.
    	 * 리프레시 토큰만 검사하고 엑세스 토큰을 재발급하는게 아니다.
    	 */
    	// 클라이언트로부터 전달 받은 리프레시 토큰이 유효한지 확인한다.
    	refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
    	// 리프레시 토큰이 유효하다면 엑세스 토큰을 재발급한다.
    	String token = jwtProvider.generateToken(refreshTokenRequest.getUsername());
    	
    	return AuthenticationResponse.builder()
    			.authenticationToken(token)
    			.refreshToken(refreshTokenRequest.getRefreshToken())
    			.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
    			.username(refreshTokenRequest.getUsername())
    			.build();
    }
}