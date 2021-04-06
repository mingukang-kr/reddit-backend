package com.reddit.comment.application;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.reddit.auth.application.AuthService;
import com.reddit.auth.application.MailContentBuilder;
import com.reddit.auth.application.MailService;
import com.reddit.auth.domain.NotificationEmail;
import com.reddit.auth.domain.User;
import com.reddit.auth.infra.UserRepository;
import com.reddit.comment.domain.Comment;
import com.reddit.comment.domain.CommentDto;
import com.reddit.comment.infra.CommentRepository;
import com.reddit.common.exception.PostNotFoundException;
import com.reddit.common.util.mapper.CommentMapper;
import com.reddit.post.domain.Post;
import com.reddit.post.infra.PostRepository;

import lombok.AllArgsConstructor;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class CommentService {
	
    private static final String POST_URL = "";
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final MailContentBuilder mailContentBuilder;
    private final MailService mailService;

    public void createComments(CommentDto commentsDto) {
    	
        Post post = postRepository.findById(commentsDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(commentsDto.getPostId().toString()));
        Comment comment = commentMapper.mapToComment(commentsDto, post, authService.getCurrentUser());
        
        commentRepository.save(comment);

        String message = mailContentBuilder.build(post.getUser().getUsername() + "님이 회원님의 글에 코멘트를 남겼습니다." + POST_URL);
        
        sendCommentNotification(message, post.getUser());
    }

    private void sendCommentNotification(String message, User user) {
    	
        mailService.sendMail(new NotificationEmail(user.getUsername() + "님이 회원님의 글에 코멘트를 남겼습니다.", user.getEmail(), message));
    }

    public List<CommentDto> getAllCommentsForPost(Long postId) {
    	
        Post post = postRepository.findById(postId)
        		.orElseThrow(() -> new PostNotFoundException(postId.toString() + "이라는 포스트를 찾을 수 없습니다."));
        
        return commentRepository.findByPost(post)
                .stream()
                .map(commentMapper::mapToCommentsDto)
                .collect(toList());
    }

    public List<CommentDto> getAllCommentsForUser(String userName) {
    	
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException(userName + "라는 이름의 사용자를 찾을 수 없습니다."));
        
        return commentRepository.findAllByUser(user)
                .stream()
                .map(commentMapper::mapToCommentsDto)
                .collect(toList());
    }
}
