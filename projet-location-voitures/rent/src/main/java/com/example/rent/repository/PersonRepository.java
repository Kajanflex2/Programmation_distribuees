package com.example.rent.repository;

import com.example.rent.data.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Person findByName(String name);
}