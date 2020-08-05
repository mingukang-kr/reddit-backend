package com.reddit.security.oauth2.dto;

import java.io.Serializable;

import com.reddit.security.oauth2.domain.CustomOAuth2User;

import lombok.Getter;

@Getter
public class SessionUser implements Serializable {

	private String name;
    private String email;
    private String picture;

    public SessionUser(CustomOAuth2User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
