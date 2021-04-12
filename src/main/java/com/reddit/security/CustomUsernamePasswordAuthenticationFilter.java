package com.reddit.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.reddit.auth.presentation.dto.LoginRequest;

@Component
public class CustomUsernamePasswordAuthenticationFilter {

	public Authentication AttemptAuthentication(LoginRequest req) throws AuthenticationException {
		String username = req.getUsername();
		String password = req.getPassword();
		
		if (username == null) {
			throw new NullPointerException("id를 입력해주세요.");
		}
		
		if (password == null) {
			throw new NullPointerException("password를 입력해주세요.");
		}
		
		return new UsernamePasswordAuthenticationToken(username, password);
	}
}