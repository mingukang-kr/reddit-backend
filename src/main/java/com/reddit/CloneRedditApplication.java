package com.reddit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import com.reddit.config.AppProperties;
import com.reddit.config.SwaggerConfiguration;

@SpringBootApplication
@Import(SwaggerConfiguration.class)
@EnableConfigurationProperties(AppProperties.class)
@EnableAsync
public class CloneRedditApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloneRedditApplication.class, args);
	}
	
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}
