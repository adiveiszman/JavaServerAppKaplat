package com.repositories;

import com.entities.TodoPostgres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoPostgresRepository extends JpaRepository<TodoPostgres, Integer> {
    List<TodoPostgres> findByTitle(String title);
    int countByState(String state);
}
