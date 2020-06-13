package com.reddit.service;

import static com.reddit.util.Constants.ACTIVATION_EMAIL;
import static java.time.Instant.now;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reddit.dto.AuthenticationResponse;
import com.reddit.dto.LoginRequest;
import com.reddit.dto.RegisterRequest;
import com.reddit.exception.SpringRedditException;
import com.reddit.model.NotificationEmail;
import com.reddit.model.User;
import com.reddit.model.VerificationToken;
import com.reddit.repository.UserRepository;
import com.reddit.repository.VerificationTokenRepository;
import com.reddit.security.JWTProvider;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final VerificationTokenRepository verificationTokenRepository;
	// 인증 메일 전송을 위한 클래스
	private final MailContentBuilder mailContentBuilder;
	private final MailService mailService;
	// security 로그인을 위한 클래스
	private final AuthenticationManager authenticationManager;
	private final JWTProvider jwtProvider;

	@Transactional
	public void signup(RegisterRequest registerRequest) {

		// 1. 가입한 회원의 정보를 DB에 저장
		User user = new User();
		user.setUsername(registerRequest.getUsername());
		user.setEmail(registerRequest.getEmail());
		user.setPassword(encodePassword(registerRequest.getPassword()));
		user.setCreated(now());
		user.setEnabled(false); // 가입 인증을 완료하면 true로 전환

		userRepository.save(user);
		log.info("가입 회원 정보가 DB에 저장되었습니다.");

		// 2. 회원 가입 완료를 위해 가입 인증 메일을 보냄
		String token = generateVerificationToken(user);
		String message = mailContentBuilder
				.build("가입 확인 인증 메일입니다. 링크를 클릭하시면 회원 가입이 완료됩니다. : " + ACTIVATION_EMAIL + "/" + token);
		mailService.sendMail(new NotificationEmail("가입을 완료하시려면 클릭하세요.", user.getEmail(), message));
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

	private String encodePassword(String password) {

		return passwordEncoder.encode(password);
	}

	public AuthenticationResponse login(LoginRequest loginRequest) {
		
		Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		
		SecurityContextHolder.getContext().setAuthentication(authenticate);
		String authenticationToken = jwtProvider.generateToken(authenticate);
		
		return new AuthenticationResponse(authenticationToken, loginRequest.getUsername());
	}

	public void verifyAccount(String token) {

		Optional<VerificationToken> verificationTokenOptional = verificationTokenRepository.findByToken(token);
		verificationTokenOptional.orElseThrow(() -> new SpringRedditException("인증 정보가 올바르지 않습니다."));
		fetchUserAndEnable(verificationTokenOptional.get());
	}

	@Transactional
	private void fetchUserAndEnable(VerificationToken verificationToken) {

		String username = verificationToken.getUser().getUsername();
		
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new SpringRedditException("해당 id를 가진 사용자를 찾을 수 없습니다. " + username));
		user.setEnabled(true); // 가입 인증을 완료하면 true로 바꿔준다.

		userRepository.save(user);
		log.info("회원 가입이 최종적으로 완료되었습니다.");
	}
}
