package com.reddit.post.presentation;

import static org.springframework.http.ResponseEntity.status;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reddit.post.application.PostService;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;

@Api(tags={"2. Post API"})
@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
public class PostController {

    private final PostService postService;   

    @PostMapping
    public ResponseEntity<Void> createPost(@RequestBody PostRequest postRequest) {
    	    	
        postService.save(postRequest);
        
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
    	
        return status(HttpStatus.OK).body(postService.getAllPosts());
    }

    @GetMapping("/{num}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long num) {
    	
        return status(HttpStatus.OK).body(postService.getPost(num));
    }
    
    @PutMapping("/{num}")
    public ResponseEntity<PostResponse> modifyPost(@RequestBody PostRequest postRequest, @PathVariable("num") long num) {
    	
    	PostResponse post = new PostResponse();
    	
    	return new ResponseEntity<PostResponse>(post, HttpStatus.OK);
    }
    
    @DeleteMapping("/{num}")
    public ResponseEntity<Void> removePost(@PathVariable("num") long num) {
    	
    	return new ResponseEntity<Void>(HttpStatus.OK); 
    }

//    @GetMapping("/by-subreddit/{id}")
//    public ResponseEntity<List<PostResponse>> getPostsBySubreddit(@PathVariable Long id) {
//    	
//        return status(HttpStatus.OK).body(postService.getPostsBySubreddit(id));
//    }

//    @GetMapping("/by-user/{username}")
//    public ResponseEntity<List<PostResponse>> getPostsByUsername(@PathVariable String username) {
//    	
//        return status(HttpStatus.OK).body(postService.getPostsByUsername(username));
//    }
}
