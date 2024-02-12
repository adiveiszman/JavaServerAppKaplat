package com.repositories;

import com.entities.TodoMongo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TodoMongoRepository extends MongoRepository<TodoMongo, Integer> {
    int countByState(String state);
    Optional<TodoMongo> findByRawid(Integer rawid);
}