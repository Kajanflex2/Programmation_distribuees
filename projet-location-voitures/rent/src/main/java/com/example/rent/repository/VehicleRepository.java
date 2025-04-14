package com.example.rent.repository;

import com.example.rent.data.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    // Récupérer tous les véhicules qui ne sont pas en location actuellement
    @Query("SELECT v FROM Vehicle v WHERE v NOT IN (SELECT r.vehicle FROM Rental r WHERE CURRENT_DATE BETWEEN r.beginDate AND r.endDate)")
    List<Vehicle> findAllAvailableVehicles();

    // Récupérer tous les véhicules par marque
    List<Vehicle> findByBrand(String brand);

    // Récupérer tous les véhicules avec un prix inférieur ou égal à
    List<Vehicle> findByPriceLessThanEqual(int price);
}