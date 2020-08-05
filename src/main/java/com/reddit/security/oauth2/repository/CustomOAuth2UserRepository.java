package com.reddit.security.oauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddit.security.oauth2.domain.CustomOAuth2User;

public interface CustomOAuth2UserRepository extends JpaRepository<CustomOAuth2User, Long> {

    Optional<CustomOAuth2User> findByEmail(String email);
}
