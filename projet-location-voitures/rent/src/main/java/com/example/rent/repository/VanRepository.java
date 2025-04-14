package com.example.rent.repository;

import com.example.rent.data.Van;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VanRepository extends JpaRepository<Van, String> {

    List<Van> findByMaxWeightGreaterThanEqual(int weight);
}