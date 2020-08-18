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
import com.reddit.security.jwt.JwtAuthenticationFilter;
import com.reddit.security.oauth2.service.CustomOAuth2UserService;

import lombok.AllArgsConstructor;

@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // jwt 인증 필터
    private final CustomAuthenticationProvider authProvider; // 시큐리티 자체 로그인 인증 필터
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
    	
    	return super.authenticationManagerBean();
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
    	
    	httpSecurity
    		.cors().and()
    		.csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/oauth2/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/posts/**").hasRole("USER")
                .antMatchers(HttpMethod.GET, "/api/subreddit").permitAll()
                .antMatchers("/v2/api-docs", "/configuration/ui",
                		"/swagger-resources/**", "/configuration/security",
                		"/swagger-ui.html", "/webjars/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login().userInfoEndpoint().userService(customOAuth2UserService);
    	
    	httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
    
    // 커스텀한 AuthenticationProivder을 ProviderManager에게 등록한다.
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	
    	auth.authenticationProvider(authProvider);
//    	auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    	// 커스텀한 UserDetailsService를 넣는 경우는 builder가 스프링이 제공하는 'DaoAuthenticationProvider'를 AuthenticationProvider로 사용한다.
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
    	
        return new BCryptPasswordEncoder();
    }
    
    /* ## 스프링 자체의 DaoAuthenticationProvider 사용하는 설정
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
    	
    	authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }
    */
}