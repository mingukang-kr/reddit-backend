package com.reddit.security.jwt;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reddit.auth.domain.RefreshToken;
import com.reddit.auth.infra.RefreshTokenRepository;
import com.reddit.common.exception.SpringRedditException;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class CustomRefreshTokenProvider {
	
	private final RefreshTokenRepository refreshTokenRepository;

	public RefreshToken generateRefreshToken() {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setToken(UUID.randomUUID().toString());
		refreshToken.setCreatedDate(Instant.now().plusMillis(9000000)); // 리프레시 토큰의 유효 기간은 엑세스 토큰보다 훨씬 길게 한다.
		
		return refreshTokenRepository.save(refreshToken);
	}

	public void validateRefreshToken(RefreshToken refreshToken) {		
		refreshTokenRepository.findByToken(refreshToken.getToken())
			.orElseThrow(() -> new SpringRedditException("부적합한 Refresh Token입니다."));
	}

	public void deleteRefreshToken(RefreshToken refreshToken) {	
		refreshTokenRepository.deleteByToken(refreshToken.getToken());
	}
}