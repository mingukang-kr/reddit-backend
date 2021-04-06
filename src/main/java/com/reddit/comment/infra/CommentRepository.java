package com.reddit.comment.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddit.auth.domain.User;
import com.reddit.comment.domain.Comment;
import com.reddit.post.domain.Post;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost(Post post);

    List<Comment> findAllByUser(User user);	
}
