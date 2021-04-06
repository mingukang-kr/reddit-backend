package com.reddit.chat.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reddit.auth.application.AuthService;
import com.reddit.chat.domain.Subreddit;
import com.reddit.chat.domain.SubredditDto;
import com.reddit.chat.infra.SubredditRepository;
import com.reddit.common.exception.SubredditNotFoundException;

import lombok.AllArgsConstructor;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class SubredditService {

    private final SubredditRepository subredditRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<SubredditDto> getAll() {
    	
        return subredditRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(toList());
    }

    @Transactional
    public SubredditDto save(SubredditDto subredditDto) {
    	
        Subreddit subreddit = subredditRepository.save(mapToSubreddit(subredditDto));
        subredditDto.setId(subreddit.getId());
        
        return subredditDto;
    }

    @Transactional(readOnly = true)
    public SubredditDto getSubreddit(Long id) {
    	
        Subreddit subreddit = subredditRepository.findById(id)
                .orElseThrow(() -> new SubredditNotFoundException(id + "를 가진 사용자를 찾을 수 없습니다."));
        
        return mapToDto(subreddit);
    }

    // Subreddit -> SubredditDTO
    private SubredditDto mapToDto(Subreddit subreddit) {
    	
        return SubredditDto.builder().name(subreddit.getName())
                .id(subreddit.getId())
                .postCount(subreddit.getPosts().size())
                .build();
    }

    // SubredditDTO -> Subreddit
    private Subreddit mapToSubreddit(SubredditDto subredditDto) {
    	
        return Subreddit.builder().name(subredditDto.getName())
                .description(subredditDto.getDescription())
                .user(authService.getCurrentUser())
                .createdDate(now()).build();
    }
}
