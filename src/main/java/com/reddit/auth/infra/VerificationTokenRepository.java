package com.reddit.auth.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddit.auth.domain.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);
}
