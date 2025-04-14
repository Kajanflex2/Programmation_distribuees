package com.example.rent.repository;

import com.example.rent.data.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, String> {

    List<Car> findByNumberOfSeatsGreaterThanEqual(int seats);
}