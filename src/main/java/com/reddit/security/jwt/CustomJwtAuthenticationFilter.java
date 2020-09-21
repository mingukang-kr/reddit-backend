package com.reddit.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class CustomJwtAuthenticationFilter extends OncePerRequestFilter {

    private CustomAccessTokenProvider jwtProvider;
    private UserDetailsService userDetailsService;

    // 로그인 후 서버 API를 요청할 때마다 JWT 인증 필터가 작동한다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
    	
        String jwt = getJwtFromRequest(request);
        log.info("jwt: {}", jwt);

        // 요청이 가진 jwt가 유효한 경우
        try {
			if (jwtProvider.validateToken(jwt)) {
			    String username = jwtProvider.getUsernameFromJWT(jwt);
			    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			    
			    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
			            null, userDetails.getAuthorities());
			    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			    SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (UsernameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        filterChain.doFilter(request, response);
    }

    // 요청으로부터 jwt를 가지고 오는 메소드
    private String getJwtFromRequest(HttpServletRequest request) {
    	
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
        	return bearerToken.substring(7);
        }
        
        return bearerToken;
    }
}
