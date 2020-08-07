package com.reddit.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.reddit.model.User;

public interface RedisUserRepository extends CrudRepository<User, String> {

    Optional<User> findByUsername(String username);
}
