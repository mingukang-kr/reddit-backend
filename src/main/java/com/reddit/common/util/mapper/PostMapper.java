package com.reddit.common.util.mapper;

import static com.reddit.vote.domain.VoteType.DOWNVOTE;
import static com.reddit.vote.domain.VoteType.UPVOTE;
import static java.time.Instant.now;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.reddit.auth.application.AuthService;
import com.reddit.chat.domain.Subreddit;
import com.reddit.comment.infra.CommentRepository;
import com.reddit.post.domain.Post;
import com.reddit.post.presentation.PostRequest;
import com.reddit.post.presentation.PostResponse;
import com.reddit.vote.domain.Vote;
import com.reddit.vote.domain.VoteType;
import com.reddit.vote.infra.VoteRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class PostMapper {

    private final CommentRepository commentRepository;
    private final AuthService authService;
    private final VoteRepository voteRepository;
	
    public PostResponse mapToPostResponse(Post post) {
    	
    	PostResponse postResponse = new PostResponse();
    	
    	postResponse.setId(post.getPostId());
    	postResponse.setPostName(post.getPostName());
    	postResponse.setUrl(post.getUrl());
    	postResponse.setDescription(post.getDescription());
    	postResponse.setVoteCount(post.getVoteCount());
    	postResponse.setSubredditName(post.getSubreddit().getName());
    	postResponse.setUserName(post.getUser().getUsername());
    	postResponse.setCommentCount(commentCount(post));
    	postResponse.setDuration(getDuration(post));
    	postResponse.setUpVote(isPostUpVoted(post));
    	postResponse.setDownVote(isPostDownVoted(post));
 
    	return postResponse;
    }
    
	public Post mapToPost(PostRequest postRequest, Subreddit subreddit) {

		return Post.builder().postId(authService.getCurrentUser().getUserId())
				.postName(postRequest.getPostName())
				.url(postRequest.getUrl())
				.description(postRequest.getDescription())
				.voteCount(0)
				.user(authService.getCurrentUser())
				.createdDate(now())
				.subreddit(subreddit).build();
	}

    Integer commentCount(Post post) {
    	
        return commentRepository.findByPost(post).size();
    }

    String getDuration(Post post) {
    	
        return TimeAgo.using(post.getCreatedDate().toEpochMilli());
    }

    boolean isPostUpVoted(Post post) {
    	
        return checkVoteType(post, UPVOTE);
    }

    boolean isPostDownVoted(Post post) {
    	
        return checkVoteType(post, DOWNVOTE);
    }

    private boolean checkVoteType(Post post, VoteType voteType) {
    	
        if (authService.isLoggedIn()) {
        	
            Optional<Vote> voteForPostByUser = voteRepository.findTopByPostAndUserOrderByVoteIdDesc(post, authService.getCurrentUser());
            
            return voteForPostByUser.filter(vote -> vote.getVoteType().equals(voteType)).isPresent();
        }
        
        return false;
    }
}
