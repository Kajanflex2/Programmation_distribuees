package com.example.rent.web;

import com.example.rent.data.Vehicle;
import com.example.rent.data.Rental;
import com.example.rent.service.RentService;
import com.example.rent.web.dto.RentalRequestDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/cars")
public class VehicleRentalController {

    private final Logger logger = LoggerFactory.getLogger(VehicleRentalController.class);

    @Autowired
    private RentService rentService;

    @PostConstruct
    public void init() {
        rentService.initData();
    }

    @GetMapping
    public List<Vehicle> getAllCars() {
        return rentService.getAllVehicles();
    }

    @GetMapping("/available")
    public List<Vehicle> getAvailableCars() {
        return rentService.getAllAvailableVehicles();
    }

    @GetMapping("/{plateNumber}")
    public ResponseEntity<Vehicle> getCarByPlateNumber(@PathVariable("plateNumber") String plateNumber) {
        return rentService.getVehicleByPlateNumber(plateNumber)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule non trouvé"));
    }

    @PutMapping("/{plateNumber}")
    public ResponseEntity<String> rentOrReturnCar(
            @PathVariable("plateNumber") String plateNumber,
            @RequestParam(value = "rent", required = true) boolean rent,
            @RequestBody(required = false) RentalRequestDTO rentalRequest) {

        logger.info("PlateNumber: " + plateNumber);
        logger.info("Rent: " + rent);

        try {
            if (rent) {
                if (rentalRequest == null) {
                    return ResponseEntity.badRequest().body("Informations de location requises");
                }

                logger.info("Rental request: " + rentalRequest);
                Rental rental = rentService.rentVehicle(plateNumber, rentalRequest);
                return ResponseEntity.ok("Véhicule loué avec succès, ID de location: " + rental.getId());
            } else {
                rentService.returnVehicle(plateNumber);
                return ResponseEntity.ok("Véhicule retourné avec succès");
            }
        } catch (Exception e) {
            logger.error("Erreur lors du traitement: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }
}