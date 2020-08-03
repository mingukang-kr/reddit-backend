package com.reddit.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.reddit.dto.LoginRequest;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CustomUsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;

	public Authentication customAttemptAuthentication(LoginRequest req) throws AuthenticationException {
		
		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
				req.getUsername(), req.getPassword());
		return authenticationManager.authenticate(authRequest);
	}
}
