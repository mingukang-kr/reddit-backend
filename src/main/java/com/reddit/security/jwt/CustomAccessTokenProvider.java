package com.reddit.security.jwt;

import static io.jsonwebtoken.Jwts.parser;
import static java.util.Date.from;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Date;
import java.time.Instant;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.reddit.common.exception.SpringRedditException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomAccessTokenProvider {
	
	// Key를 만들어 JKS에 저장 후 사용해보는 방식으로 구현.
	private KeyStore keyStore;
	@Value("${jwt.expiration.time}")
	private Long accessTokenExpiration;

	@PostConstruct
	public void init() {
		try {
			keyStore = KeyStore.getInstance("JKS");
			InputStream resourceAsStream = getClass().getResourceAsStream("/springblog.jks");
			keyStore.load(resourceAsStream, "123456".toCharArray()); // input 스트림과 비밀번호를 매개변수로 전달한다.
		} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			throw new SpringRedditException("키 저장소를 불러오는 중 오류가 발생했습니다.");
		}
	}

	// Authentication 객체로 Access 토큰 생성
	public String generateToken(Authentication authentication) {
		User principal = (User)authentication.getPrincipal(); // 커스텀 User 클래스가 아님에 주의
		
		return Jwts.builder()
				.setSubject(principal.getUsername())
				.signWith(getPrivateKey())
				.setExpiration(Date.from(Instant.now().plusMillis(accessTokenExpiration)))
				.compact();
	}
	
	// Username으로 Access 토큰 생성
	public String generateToken(String username) {
		return Jwts.builder()
				.setSubject(username)
				.setIssuedAt(from(Instant.now()))
				.signWith(getPrivateKey())
				.setExpiration(Date.from(Instant.now().plusMillis(accessTokenExpiration)))
				.compact();
	}
	
    public boolean validateToken(String jwt) {
		try {
			parser().setSigningKey(getPublickey())
				.parseClaimsJws(jwt);
	    	return true;
		} catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
			throw new BadCredentialsException("토큰의 인증 정보가 맞지 않습니다.", e);
		} catch (ExpiredJwtException e) {
			throw e; // Refresh Token 요청 작업 필요
		}
    }

	private PrivateKey getPrivateKey() {
		try {
			return (PrivateKey)keyStore.getKey("springblog", "123456".toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
			e.printStackTrace();			
			throw new SpringRedditException("개인키를 가지고 오는 중 오류가 발생했습니다.");
		}
	}
	
    private PublicKey getPublickey() {
    	try {
            return keyStore.getCertificate("springblog").getPublicKey();
        } catch (KeyStoreException e) {
            throw new SpringRedditException("공개키를 가지고 오던 중 오류가 발생하였습니다.");
        }
    }

    public String getUsernameFromJWT(String token) {
    	Claims claims = parser()
                .setSigningKey(getPublickey())
                .parseClaimsJws(token)
                .getBody();
    	log.info("claims : {}", claims.toString());
        return claims.getSubject();
    }
    
    public Long getJwtExpirationInMillis() {
    	return accessTokenExpiration;
    }
}