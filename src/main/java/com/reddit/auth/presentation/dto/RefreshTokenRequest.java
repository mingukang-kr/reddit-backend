package com.reddit.auth.presentation.dto;

import javax.validation.constraints.NotBlank;

import com.reddit.auth.domain.RefreshToken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
	
	@NotBlank
	private RefreshToken refreshToken;
    private String username;
}
