package com.reddit.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.reddit.model.User;
import com.reddit.repository.RedisUserRepository;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RedisTest {
    
    @Autowired
    private RedisUserRepository redisUserRepository;
    
    @Test
    public void test_login() {
    	
    	User user = new User(1L, "mingu", "1234", "redis@email.com", Instant.now(), true, com.reddit.model.User.Authority.ROLE_ADMIN);
    	redisUserRepository.save(user);
    	
        User loadedUser = redisUserRepository.findByUsername("mingu").get();
        log.info(loadedUser.toString());
        assertThat(loadedUser.getUsername()).isEqualTo("mingu");
    }
}
