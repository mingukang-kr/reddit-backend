package com.reddit.post.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddit.auth.domain.User;
import com.reddit.chat.domain.Subreddit;
import com.reddit.post.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	List<Post> findAllBySubreddit(Subreddit subreddit);

    List<Post> findByUser(User user);
}
