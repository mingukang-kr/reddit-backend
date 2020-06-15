package com.reddit.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.reddit.exception.SpringRedditException;

import io.jsonwebtoken.Jwts;

@Service
public class JWTProvider {
	
	private KeyStore keyStore;

	@PostConstruct
	public void init() {
		
		try {
			keyStore = KeyStore.getInstance("JKS");
			InputStream resourceAsStream = getClass().getResourceAsStream("/springblog.jks");
			keyStore.load(resourceAsStream, "123456".toCharArray()); // input 스트림과 비밀번호를 매개변수로 전달한다.
		} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
			throw new SpringRedditException("keyStore를 불러오는 중 오류가 발생했습니다.");
		}
	}

	public String generateToken(Authentication authentication) {
		
		User principal = (User)authentication.getPrincipal(); // security의 userDetails 패키지의 User 클래스임에 주의
		
		return Jwts.builder().setSubject(principal.getUsername()).signWith(getPrivateKey()).compact();
	}

	private PrivateKey getPrivateKey() {
		
		try {
			return (PrivateKey)keyStore.getKey("springblog", "123456".toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
			throw new SpringRedditException("Exception occured while retrieving public key from keystore");
		}
	}
}