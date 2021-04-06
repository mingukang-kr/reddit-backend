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

	@ApiOperation(value = "회원 가입", notes = "회원 가입을 신청한다.")
	@PostMapping(value = "/signup", produces = "application/json; charset=UTF-8")
	public ResponseEntity<String> signup(@RequestBody RegisterRequest registerRequest) {
		authService.signup(registerRequest);
		return ResponseEntity.ok().body("가입 인증 메일을 보냈습니다. 메일을 확인하여 회원 가입을 완료하여 주세요.");
	}
	
    @ApiOperation(value = "가입 계정 인증", notes = "가입한 메일로 보낸 링크를 통해서 회원 가입을 완료한다.")
	@GetMapping(value = "/accountVerification/{token}", produces = "application/json; charset=UTF-8")
	public ResponseEntity<String> verifyAccount(@PathVariable String token) {
		authService.verifyAccount(token);
		return ResponseEntity.status(OK).body("회원 가입이 완료되었습니다.");
	}
	
	@ApiOperation(value = "로그인", notes = "로그인")
    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
	
    @ApiOperation(value = "엑세스 토큰 갱신", notes = "만료된 엑세스 토큰을 갱신한다.")
	@PostMapping("/refresh/token")
	public AuthenticationResponse refreshTokens(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
		return authService.refreshToken(refreshTokenRequest);
	}
	
    @ApiOperation(value = "로그아웃", notes = "로그아웃")
	@PostMapping(value = "/logout", produces = "application/json; charset=UTF-8")
	public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
		refreshTokenService.deleteRefreshToken(refreshTokenRequest.getRefreshToken());
		return ResponseEntity.status(OK).body("로그아웃 되었습니다.");
	}
}