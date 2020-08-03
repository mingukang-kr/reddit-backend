package com.reddit.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.reddit.dto.LoginRequest;

@Component
public class CustomUsernamePasswordAuthenticationFilter {

	public Authentication customAttemptAuthentication(LoginRequest req) throws AuthenticationException {
		
		return new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());
	}
}
