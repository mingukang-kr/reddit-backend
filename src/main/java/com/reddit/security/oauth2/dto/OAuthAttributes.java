package com.reddit.security.oauth2.dto;

import java.util.Map;

import com.reddit.security.oauth2.domain.CustomOAuth2User;
import com.reddit.security.oauth2.domain.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthAttributes {

	private Map<String, Object> attributes;
	private String nameAttributeKey;
	private String name;
	private String email;
	private String picture;

	@Builder
	public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email,
			String picture) {

		this.attributes = attributes;
		this.nameAttributeKey = nameAttributeKey;
		this.name = name;
		this.email = email;
		this.picture = picture;
	}

	// 현재는 네이버와 구글만 등록되어있지만 다른 외부 서비스들도 여기서 추가하여 바꿔주면 됩니다.
	public static OAuthAttributes from(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
		if ("naver".equals(registrationId)) {
			return fromNaver("id", attributes);
		}
		return fromGoogle(userNameAttributeName, attributes);
	}

	// 구글
	private static OAuthAttributes fromGoogle(String userNameAttributeName, Map<String, Object> attributes) {

		return OAuthAttributes.builder()
				.name((String) attributes.get("name")).email((String) attributes.get("email"))
				.picture((String) attributes.get("picture")).attributes(attributes)
				.nameAttributeKey(userNameAttributeName).build();
	}

	// 네이버
	private static OAuthAttributes fromNaver(String userNameAttributeName, Map<String, Object> attributes) {
		
		@SuppressWarnings("unchecked")
		Map<String, Object> response = (Map<String, Object>)attributes.get("response");

		return OAuthAttributes.builder()
				.name((String) response.get("name")).email((String) response.get("email"))
				.picture((String) response.get("profile_image")).attributes(response)
				.nameAttributeKey(userNameAttributeName).build();
	}

	public CustomOAuth2User toEntity() {

		return CustomOAuth2User.builder().name(name).email(email).picture(picture).role(Role.GUEST).build();
	}
}
