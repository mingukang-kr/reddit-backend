package com.reddit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
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
    		.authorizeRequests()
    			.antMatchers("/api/auth/**").permitAll()
                .antMatchers("/oauth2/**").permitAll()
                .antMatchers("/v2/api-docs", "/configuration/ui",
                		"/swagger-resources/**", "/configuration/security",
                		"/swagger-ui.html", "/webjars/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/posts/**").hasRole("USER")
                .antMatchers(HttpMethod.GET, "/api/subreddit").hasRole("USER")
            .anyRequest().authenticated()
            	.and()
            .oauth2Login()
                .userInfoEndpoint()
                	.userService(customOAuth2UserService);
    	
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