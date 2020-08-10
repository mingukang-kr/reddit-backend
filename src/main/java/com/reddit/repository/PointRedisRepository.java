package com.reddit.repository;

import org.springframework.data.repository.CrudRepository;

import com.reddit.model.Point;

public interface PointRedisRepository extends CrudRepository<Point, String> {

}
