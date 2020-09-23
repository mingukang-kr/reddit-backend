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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.reddit.exception.SpringRedditException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomAccessTokenProvider {
	
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

	public String generateToken(Authentication authentication) {
		User principal = (User)authentication.getPrincipal(); // security의 userDetails 패키지의 User 클래스임에 주의
		
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
	
    public boolean validateToken(String jwt) throws Exception {
    	Jws<Claims> claims = parser()
    			.setSigningKey(getPublickey())
    			.parseClaimsJws(jwt);
    	log.info("claims : {}", claims.toString());
    	
    	// jwt 서명이 일치하는지 확인한다. (jwt 조작 여부)
    	String sign = jwt.split("\\.")[2];
    	if ( !claims.getSignature().equals(sign) ) {
    		log.info("jwt 서명 불일치");
    		throw new Exception("jwt 서명이 일치하지 않습니다.");
    	}
    	
    	// jwt의 만료 시간이 다 되었는지 확인한다.
    	if ( claims.getBody().getExpiration().before(Date.from(Instant.now())) ) {
    		log.info("jwt 만료");
    		return false;
    	}
    	
    	return true;
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
            throw new SpringRedditException("키 저장소에서 공개키를 가지고 오던 중 오류가 발생하였습니다.");
        }
    }

    public String getUsernameFromJWT(String token) {
    	Claims claims = parser()
                .setSigningKey(getPublickey())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
    
    public Long getJwtExpirationInMillis() {
    	return accessTokenExpiration;
    }
}