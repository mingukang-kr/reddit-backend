package com.reddit.chat.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddit.chat.domain.Subreddit;

public interface SubredditRepository extends JpaRepository<Subreddit, Long> {

    Optional<Subreddit> findByName(String subredditName);
}
