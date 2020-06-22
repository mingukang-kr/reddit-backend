package com.reddit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import com.reddit.config.SwaggerConfiguration;

@SpringBootApplication
@Import(SwaggerConfiguration.class)
@EnableAsync
public class SpringbootWebservicePrac1Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootWebservicePrac1Application.class, args);
	}

}
