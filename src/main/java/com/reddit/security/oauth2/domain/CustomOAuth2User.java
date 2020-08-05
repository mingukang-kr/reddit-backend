package com.reddit.security.oauth2.domain;

import javax.persistence.*;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class CustomOAuth2User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String email;

	@Column
	private String picture;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Builder
	public CustomOAuth2User(String name, String email, String picture, Role role) {

		this.name = name;
		this.email = email;
		this.picture = picture;
		this.role = role;
	}

	public CustomOAuth2User update(String name, String picture) {

		this.name = name;
		this.picture = picture;

		return this;
	}

	public String getRoleKey() {

		return this.role.getKey();
	}
}
