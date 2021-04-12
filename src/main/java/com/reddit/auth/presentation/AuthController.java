package com.reddit.auth.presentation;

import static org.springframework.http.HttpStatus.OK;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reddit.auth.application.AuthService;
import com.reddit.auth.presentation.dto.AuthenticationResponse;
import com.reddit.auth.presentation.dto.LoginRequest;
import com.reddit.auth.presentation.dto.RefreshTokenRequest;
import com.reddit.auth.presentation.dto.RegisterRequest;
import com.reddit.security.jwt.CustomRefreshTokenProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@Api(tags={"1. Auth API"})
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final CustomRefreshTokenProvider refreshTokenService;

	@ApiOperation(value = "회원 가입")
	@PostMapping("/sign-up")
	public ResponseEntity<String> signup(@RequestBody RegisterRequest registerRequest) {
		String token = authService.signup(registerRequest);
		return ResponseEntity.ok().body(token);
	}
	
    @ApiOperation(value = "가입 계정 인증")
	@GetMapping("/account-verification/{token}")
	public ResponseEntity<Long> verifyAccount(@PathVariable String token) {
		Long userId = authService.verifyAccount(token);
		return ResponseEntity.status(OK).body(userId);
	}
	
	@ApiOperation(value = "로그인")
    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
	
    @ApiOperation(value = "엑세스 토큰 갱신", notes = "만료된 엑세스 토큰을 갱신한다.")
	@PostMapping("/token-renewal")
	public AuthenticationResponse refreshTokens(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
		return authService.refreshToken(refreshTokenRequest);
	}
	
    @ApiOperation(value = "로그아웃")
	@PostMapping("/logout")
	public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
		refreshTokenService.deleteRefreshToken(refreshTokenRequest.getRefreshToken());
		return ResponseEntity.status(OK).body("로그아웃 되었습니다.");
	}
}