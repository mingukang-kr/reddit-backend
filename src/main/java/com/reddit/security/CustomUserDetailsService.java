package com.reddit.security;

import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reddit.auth.domain.User;
import com.reddit.auth.infra.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;
	
    @Override
    @Transactional(readOnly = true)
    //@Cacheable(value = "user", unless = "#result == null")
    public UserDetails loadUserByUsername(String username) {
    	Optional<User> userOptional = userRepository.findByUsername(username);
        User user = userOptional
                .orElseThrow(() -> new UsernameNotFoundException(username + " 라는 이름의 회원을 찾을 수 없습니다."));
        
        return new org.springframework.security.core.userdetails.User(
        		user.getUsername(), user.getPassword(),
                user.isEnabled(), true, true, true, getAuthorities(user.getAuthority().toString()));
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return singletonList(new SimpleGrantedAuthority(role));
    }
}
