package com.reddit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.reddit.security.CustomAuthenticationProvider;
import com.reddit.security.jwt.CustomJwtAuthenticationFilter;
import com.reddit.security.oauth2.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomJwtAuthenticationFilter jwtAuthenticationFilter; // 커스텀 JWT 인증 필터
    private final CustomAuthenticationProvider authProvider; // 커스텀 AuthenticationProvider
    private final CustomOAuth2UserService customOAuth2UserService; // 커스텀 OAuth 처리

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {	
    	return super.authenticationManagerBean();
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
    	httpSecurity
    		.cors()
    			.and()
    		.csrf().disable()
    		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    			.and()
    		/*
    		 * permitAll() 은 인증 필요없이 요청이 가능한 것이고, authenticated()는 인증이 필요하다는 것이다. => 서로 다름을 명심!
    		 */
    		.authorizeRequests()
    			.antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/posts/**").hasAnyRole("ADMIN", "USER")
                .antMatchers("/api/subreddit/**").hasAnyRole("ADMIN", "USER")
            .anyRequest().authenticated()
            	.and()
            .oauth2Login()
                .userInfoEndpoint().userService(customOAuth2UserService);
    	
    	// JWT 인증 필터를 Security form 인증 필터 앞에 둬서 전처리하게한다.
    	httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	// 커스텀 AuthenticationProvider을 ProviderManager에 등록한다.
    	auth.authenticationProvider(authProvider);
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}