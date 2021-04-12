package com.reddit.auth.application;

import static com.reddit.common.Constants.ACTIVATION_EMAIL;
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

import com.reddit.auth.domain.NotificationEmail;
import com.reddit.auth.domain.User;
import com.reddit.auth.domain.VerificationToken;
import com.reddit.auth.domain.User.Authority;
import com.reddit.auth.infra.UserRepository;
import com.reddit.auth.infra.VerificationTokenRepository;
import com.reddit.auth.presentation.dto.AuthenticationResponse;
import com.reddit.auth.presentation.dto.LoginRequest;
import com.reddit.auth.presentation.dto.RefreshTokenRequest;
import com.reddit.auth.presentation.dto.RegisterRequest;
import com.reddit.common.exception.SpringRedditException;
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
	private final CustomRefreshTokenProvider refreshTokenProvider;
	private final CustomUsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter;
	private final CustomAuthenticationProvider customAuthenticationProvider;
	
	@Transactional
	public String signup(RegisterRequest registerRequest) {
		// 1. 가입 신청한 회원의 정보를 DB에 저장
		User user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		user.setCreated(now());
		user.setEnabled(false); // 메일 인증을 통해 최종 회원 가입이 될 시에 true로 바꿔준다.
		
		userRepository.save(user);
		
		log.info("가입 신청 회원의 정보가 DB에 저장되었습니다.");

		// 2. 가입 완료를 위해 인증 메일 전송
		String token = generateVerificationToken(user);
		String message = mailContentBuilder.build("가입 인증 메일입니다. 링크를 클릭하시면 회원 가입이 완료됩니다. " 
				+ ACTIVATION_EMAIL + "/" + token);
		mailService.sendMail(new NotificationEmail("가입을 완료하시려면 클릭하세요.", user.getEmail(), message));
		
		return token;
	}
	
	public AuthenticationResponse login(LoginRequest loginRequest) {
		// spring security
		Authentication needToBeAuthenticated = customUsernamePasswordAuthenticationFilter.AttemptAuthentication(loginRequest);
		Authentication authenticated = customAuthenticationProvider.authenticate(needToBeAuthenticated);
		SecurityContextHolder.getContext().setAuthentication(authenticated);
		
		// jwt
		String accessToken = jwtProvider.generateToken(authenticated);
		String refreshToken = refreshTokenProvider.generateRefreshToken().getToken();

		return AuthenticationResponse.builder()
				.authenticationToken(accessToken)
				.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
				.refreshToken(refreshToken)
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
	public Long verifyAccount(String token) {
		Optional<VerificationToken> verificationTokenOptional = verificationTokenRepository.findByToken(token);
		verificationTokenOptional
			.orElseThrow(() -> new SpringRedditException("인증 정보가 올바르지 않습니다."));
		
		return fetchUserAndEnable(verificationTokenOptional.get());
	}

	// 인증된 토큰으로부터 사용자 정보를 DB에서 불러오는 메소드
	@Transactional
	private Long fetchUserAndEnable(VerificationToken verificationToken) {
		User user = verificationToken.getUser();
		user.setEnabled(true); // 가입 인증을 완료하면 true로 바꿔준다.
		user.setAuthority(Authority.ROLE_USER);

		User userSignCompleted = userRepository.save(user);
		
		log.info("회원 가입이 완료되었습니다.");
		
		return userSignCompleted.getUserId();
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
    	refreshTokenProvider.validateRefreshToken(refreshTokenRequest.getRefreshToken());
    	// 리프레시 토큰이 유효하다면 엑세스 토큰을 재발급한다.
    	String token = jwtProvider.generateToken(refreshTokenRequest.getUsername());
    	
    	return AuthenticationResponse.builder()
    			.authenticationToken(token)
    			.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
    			.refreshToken(refreshTokenRequest.getRefreshToken().getToken())
    			.username(refreshTokenRequest.getUsername())
    			.build();
    }
}