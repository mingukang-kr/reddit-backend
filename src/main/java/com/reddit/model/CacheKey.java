package com.reddit.model;

public class CacheKey {
	
	// 기본 캐시 key 지속 시간
    public static final int DEFAULT_EXPIRE_SEC = 60;
    
    // User 캐시 key 및 지속 시간
    public static final String USER = "user";
    public static final int USER_EXPIRE_SEC = 60 * 5;
    
    // Post 캐시 key 및 지속 시간
    public static final String POST = "post";
    public static final String POSTS = "posts";
    public static final int POST_EXPIRE_SEC = 60 * 5;
}
