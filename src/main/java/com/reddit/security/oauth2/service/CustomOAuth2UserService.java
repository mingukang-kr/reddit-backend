package com.reddit.security.oauth2.service;

import java.util.Collections;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.reddit.security.oauth2.domain.CustomOAuth2User;
import com.reddit.security.oauth2.dto.OAuthAttributes;
import com.reddit.security.oauth2.dto.SessionUser;
import com.reddit.security.oauth2.repository.CustomOAuth2UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final CustomOAuth2UserRepository customOAuth2UserRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        
        // 1. 외부 API로부터 받은 회원 정보(userRequest)로부터 각종 데이터를 담는다.
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
        		.getUserInfoEndpoint().getUserNameAttributeName();

        // 2. 어떤 외부 API(registrationId)로부터 온 정보인지 파악하고, 그에 맞게 다시 데이터를 담는다.
        OAuthAttributes attributes = OAuthAttributes.from(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 3. DB에 회원 정보를 저장하고, 세션에 OAuth 유저 객체를 담는다.
        CustomOAuth2User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user));

        // 4. 유저의 권한, 속성, 키를 OAuth2User에 담아서 리턴한다.
        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(), attributes.getNameAttributeKey());
    }

    private CustomOAuth2User saveOrUpdate(OAuthAttributes attributes) {

    	CustomOAuth2User user = customOAuth2UserRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());

        return customOAuth2UserRepository.save(user);
    }
}
