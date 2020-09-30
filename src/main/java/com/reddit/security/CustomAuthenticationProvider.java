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

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
	
	private final CustomUserDetailsService customUserDetailsService;

	@Override
	public Authentication authenticate(Authentication needToBeAuthenticated) throws AuthenticationException {
		if (needToBeAuthenticated == null) {
			throw new InternalAuthenticationServiceException("인증 정보가 null값입니다.");
		} 
		String username = needToBeAuthenticated.getName();
		
		if (needToBeAuthenticated.getCredentials() == null) {
			throw new AuthenticationCredentialsNotFoundException("비밀 번호가 null값입니다.");
		}
		String password = needToBeAuthenticated.getCredentials().toString();
		
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
		// 비밀번호를 올바르게 입력했는지 확인
		if (!new BCryptPasswordEncoder().matches(password, loadedUser.getPassword())) { 
			throw new BadCredentialsException("Password does not match stored value"); 
		}
		if (!loadedUser.isCredentialsNonExpired()) { 
			throw new CredentialsExpiredException("User credentials have expired"); 
		}
		
		// 인증에 성공하면 토큰을 발행한다.
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(loadedUser, null, loadedUser.getAuthorities()); // 인증된 토큰에는 비밀번호는 웬만하면 null을 넣는다.
		result.setDetails(needToBeAuthenticated.getDetails());
		return result;
	}

	// 인증을 맡을 provider를 구현한 것이므로 인증 가능 여부를 판단하는 supports()는 미구현.
	@Override
	public boolean supports(Class<?> authentication) {
		return false;
	}
}