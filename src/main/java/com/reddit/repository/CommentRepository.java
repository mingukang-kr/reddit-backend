package com.reddit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reddit.model.Comment;
import com.reddit.model.Post;
import com.reddit.model.User;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost(Post post);

    List<Comment> findAllByUser(User user);	
}
