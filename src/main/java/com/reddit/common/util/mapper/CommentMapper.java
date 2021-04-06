package com.reddit.common.util.mapper;

import static java.time.Instant.now;

import org.springframework.stereotype.Component;

import com.reddit.auth.domain.User;
import com.reddit.comment.domain.Comment;
import com.reddit.comment.domain.CommentDto;
import com.reddit.post.domain.Post;

@Component
public class CommentMapper {

	public Comment mapToComment(CommentDto commentsDto, Post post, User user) {
		
		Comment comment = new Comment();
		
		comment.setId(commentsDto.getId());
		comment.setText(commentsDto.getText());
		comment.setCreatedDate(now());
		comment.setPost(post);
		comment.setUser(user);
		
		return comment;
	}
	
	public CommentDto mapToCommentsDto(Comment comment) {
		
		CommentDto commentsDto = new CommentDto();
		
		commentsDto.setId(comment.getPost().getPostId());
		commentsDto.setUserName(comment.getUser().getUsername());
		
		return commentsDto;
	}
}
