package com.reddit.vote.application;

import static com.reddit.vote.domain.VoteType.UPVOTE;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reddit.auth.application.AuthService;
import com.reddit.common.exception.PostNotFoundException;
import com.reddit.common.exception.SpringRedditException;
import com.reddit.post.domain.Post;
import com.reddit.post.infra.PostRepository;
import com.reddit.vote.domain.Vote;
import com.reddit.vote.domain.VoteDto;
import com.reddit.vote.infra.VoteRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final AuthService authService;

    @Transactional
    public void vote(VoteDto voteDto) {
    	
        Post post = postRepository.findById(voteDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(voteDto.getPostId() + "라는 포스트를 찾을 수 없습니다."));
        
        Optional<Vote> voteByPostAndUser = voteRepository.findTopByPostAndUserOrderByVoteIdDesc(post, authService.getCurrentUser());
        
        if (voteByPostAndUser.isPresent() && voteByPostAndUser.get().getVoteType().equals(voteDto.getVoteType())) {	
            throw new SpringRedditException("이미 찬반 의견을 표시하셨습니다.");
        }
        
        if (UPVOTE.equals(voteDto.getVoteType())) {
            post.setVoteCount(post.getVoteCount() + 1);
        } else {
            post.setVoteCount(post.getVoteCount() - 1);
        }
        
        voteRepository.save(mapToVote(voteDto, post));
        postRepository.save(post);
    }

    private Vote mapToVote(VoteDto voteDto, Post post) {
    	
        return Vote.builder()
                .voteType(voteDto.getVoteType())
                .post(post)
                .user(authService.getCurrentUser())
                .build();
    }
}
