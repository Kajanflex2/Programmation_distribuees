package com.example.rent.repository;

import com.example.rent.data.Rental;
import com.example.rent.data.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByVehiclePlateNumber(String plateNumber);

    List<Rental> findByPersonId(Long personId);

    @Query("SELECT r FROM Rental r WHERE r.vehicle.plateNumber = :plateNumber AND :date BETWEEN r.beginDate AND r.endDate")
    List<Rental> findActiveRentalsByVehiclePlateNumberAndDate(@Param("plateNumber") String plateNumber, @Param("date") Date date);

    @Query("SELECT r FROM Rental r WHERE :date BETWEEN r.beginDate AND r.endDate")
    List<Rental> findActiveRentalsOnDate(@Param("date") Date date);
}