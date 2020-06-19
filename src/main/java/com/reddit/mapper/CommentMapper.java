package com.reddit.mapper;

import static java.time.Instant.now;

import org.springframework.stereotype.Component;

import com.reddit.dto.CommentsDto;
import com.reddit.model.Comment;
import com.reddit.model.Post;
import com.reddit.model.User;

@Component
public class CommentMapper {

	public Comment mapToComment(CommentsDto commentsDto, Post post, User user) {
		
		Comment comment = new Comment();
		
		comment.setId(commentsDto.getId());
		comment.setText(commentsDto.getText());
		comment.setCreatedDate(now());
		comment.setPost(post);
		comment.setUser(user);
		
		return comment;
	}
	
	public CommentsDto mapToCommentsDto(Comment comment) {
		
		CommentsDto commentsDto = new CommentsDto();
		
		commentsDto.setId(comment.getPost().getPostId());
		commentsDto.setUserName(comment.getUser().getUsername());
		
		return commentsDto;
	}
}
