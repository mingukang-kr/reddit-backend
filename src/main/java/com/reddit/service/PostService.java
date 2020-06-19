package com.reddit.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reddit.dto.PostRequest;
import com.reddit.dto.PostResponse;
import com.reddit.exception.PostNotFoundException;
import com.reddit.exception.SubredditNotFoundException;
import com.reddit.mapper.PostMapper;
import com.reddit.model.Post;
import com.reddit.model.Subreddit;
import com.reddit.model.User;
import com.reddit.repository.PostRepository;
import com.reddit.repository.SubredditRepository;
import com.reddit.repository.UserRepository;

import lombok.AllArgsConstructor;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Transactional
public class PostService {

	private final PostRepository postRepository;
	private final SubredditRepository subredditRepository;
	private final UserRepository userRepository;
	private final PostMapper postMapper;

	public void save(PostRequest postRequest) {

		Subreddit subreddit = subredditRepository.findByName(postRequest.getSubredditName()).orElseThrow(
				() -> new SubredditNotFoundException(postRequest.getSubredditName() + "라는 reddit을 찾을 수 없습니다."));

		postRepository.save(postMapper.mapToPost(postRequest, subreddit));
	}

	@Transactional(readOnly = true)
	public List<PostResponse> getAllPosts() {

		return postRepository.findAll()
				.stream()
				.map(postMapper::mapToPostResponse)
				.collect(toList());
	}

	@Transactional(readOnly = true)
	public PostResponse getPost(Long id) {
		
		Post post = postRepository.findById(id)
				.orElseThrow(() -> new PostNotFoundException(id.toString()));
		
		return postMapper.mapToPostResponse(post);
	}

	@Transactional(readOnly = true)
	public List<PostResponse> getPostsBySubreddit(Long subredditId) {
		
		Subreddit subreddit = subredditRepository.findById(subredditId)
				.orElseThrow(() -> new SubredditNotFoundException(subredditId.toString()));
		
		List<Post> posts = postRepository.findAllBySubreddit(subreddit);
		
		return posts
				.stream()
				.map(postMapper::mapToPostResponse)
				.collect(toList());
	}

	@Transactional(readOnly = true)
	public List<PostResponse> getPostsByUsername(String username) {
		
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException(username));
		
		return postRepository.findByUser(user)
				.stream()
				.map(postMapper::mapToPostResponse)
				.collect(toList());
	}
}
