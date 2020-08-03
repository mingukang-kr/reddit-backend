package com.reddit.security;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
	
	private final CustomUserDetailsService customUserDetailsService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		
		if (authentication == null) {
			throw new InternalAuthenticationServiceException("인증 정보가 null값입니다.");
		} 
		String username = authentication.getName();
		
		if (authentication.getCredentials() == null) {
			throw new AuthenticationCredentialsNotFoundException("비밀 번호가 null값입니다.");
		}
		String password = authentication.getCredentials().toString();
		// DB에서 유저 정보를 불러온다.
		UserDetails loadedUser = customUserDetailsService.loadUserByUsername(username);
		
		if (loadedUser == null) {
			throw new InternalAuthenticationServiceException("UserDetailsService returned null, which is an interface contract violation");
		}
		if (!loadedUser.isAccountNonLocked()) { 
			throw new LockedException("User account is locked");
		}
		if (!loadedUser.isEnabled()) {
			throw new DisabledException("User is disabled");
		}
		if (!loadedUser.isAccountNonExpired()) {
			throw new AccountExpiredException("User account has expired");
		}
		// 실질적인 인증 -> DB의 유저 정보와 입력한 유저 정보가 일치하는지 비교한다.
		if (!new BCryptPasswordEncoder().matches(password, loadedUser.getPassword())) { 
			throw new BadCredentialsException("Password does not match stored value"); 
		}
		if (!loadedUser.isCredentialsNonExpired()) { 
			throw new CredentialsExpiredException("User credentials have expired"); 
		}
		
		// 인증 완료 
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(loadedUser, null, loadedUser.getAuthorities());
		result.setDetails(authentication.getDetails());
		return result;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		// TODO Auto-generated method stub
		return false;
	}
}
