package com.reddit.auth.presentation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reddit.auth.application.AuthService;
import com.reddit.auth.infra.UserRepository;
import com.reddit.auth.infra.VerificationTokenRepository;
import com.reddit.auth.presentation.dto.RegisterRequest;
import com.reddit.security.jwt.CustomRefreshTokenProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private VerificationTokenRepository verificationTokenRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@MockBean
    private AuthService authService;
	
	@MockBean
    private CustomRefreshTokenProvider customRefreshTokenProvider;

	@Test
	void 회원가입() throws Exception {
		RegisterRequest registerRequest = new RegisterRequest("tester", "test@test.com", "1234");
		String request = objectMapper.writeValueAsString(registerRequest);

		mockMvc.perform(post("/api/auth/sign-up")
				.header("Content-Type", MediaType.APPLICATION_JSON)
				.content(request)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
}
