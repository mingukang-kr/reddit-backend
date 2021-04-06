package com.reddit.vote.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddit.auth.domain.User;
import com.reddit.post.domain.Post;
import com.reddit.vote.domain.Vote;

public interface VoteRepository extends JpaRepository<Vote, Long> {

	Optional<Vote> findTopByPostAndUserOrderByVoteIdDesc(Post post, User currentUser);
}
