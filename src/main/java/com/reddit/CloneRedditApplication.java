package com.reddit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import com.reddit.config.SwaggerConfiguration;

@SpringBootApplication
@Import(SwaggerConfiguration.class)
@EnableAsync
public class CloneRedditApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloneRedditApplication.class, args);
	}
}