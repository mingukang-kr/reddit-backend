package com.reddit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringbootWebservicePrac1Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootWebservicePrac1Application.class, args);
	}

}
