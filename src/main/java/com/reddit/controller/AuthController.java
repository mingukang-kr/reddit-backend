package com.reddit.controller;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reddit.dto.AuthenticationResponse;
import com.reddit.dto.LoginRequest;
import com.reddit.dto.RegisterRequest;
import com.reddit.service.AuthService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/mingunet")
@AllArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity signup(@RequestBody RegisterRequest registerRequest) {

		authService.signup(registerRequest);

		return new ResponseEntity(OK);
	}
	
    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody LoginRequest loginRequest) {
    	
        return authService.login(loginRequest);
    }

	@GetMapping("accountVerification/{token}")
	public ResponseEntity<String> verifyAccount(@PathVariable String token) {
		
		authService.verifyAccount(token);
		
		return new ResponseEntity<>("회원 가입 완료", OK);
	}
}
