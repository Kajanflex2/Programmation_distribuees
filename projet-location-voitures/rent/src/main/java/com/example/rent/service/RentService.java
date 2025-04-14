package com.example.rent.service;

import com.example.rent.data.*;
import com.example.rent.repository.*;
import com.example.rent.web.dto.RentalRequestDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class RentService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private VanRepository vanRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RentalRepository rentalRepository;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    // Initialisation de données test
    @Transactional
    public void initData() {
        // Véhicules
        Car car1 = new Car("11AA22", "Ferrari", 1000, 2);
        Car car2 = new Car("33BB44", "Porsche", 900, 2);
        Car car3 = new Car("55CC66", "Peugeot", 100, 5);

        Van van1 = new Van("77DD88", "Mercedes", 500, 1500);
        Van van2 = new Van("99EE00", "Renault", 300, 1000);

        carRepository.saveAll(List.of(car1, car2, car3));
        vanRepository.saveAll(List.of(van1, van2));

        // Personnes
        Person person1 = new Person("Jean Dupont");
        Person person2 = new Person("Marie Martin");

        personRepository.saveAll(List.of(person1, person2));
    }

    // Récupérer tous les véhicules
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // Récupérer tous les véhicules disponibles
    public List<Vehicle> getAllAvailableVehicles() {
        return vehicleRepository.findAllAvailableVehicles();
    }

    // Récupérer un véhicule par son immatriculation
    public Optional<Vehicle> getVehicleByPlateNumber(String plateNumber) {
        return vehicleRepository.findById(plateNumber);
    }

    // Louer un véhicule
    @Transactional
    public Rental rentVehicle(String plateNumber, RentalRequestDTO rentalRequest) throws Exception {
        // Vérifier si le véhicule existe
        Vehicle vehicle = vehicleRepository.findById(plateNumber)
                .orElseThrow(() -> new Exception("Véhicule non trouvé: " + plateNumber));

        // Convertir les dates
        Date beginDate;
        Date endDate;
        try {
            beginDate = dateFormat.parse(rentalRequest.getBegin());
            endDate = dateFormat.parse(rentalRequest.getEnd());
        } catch (ParseException e) {
            throw new Exception("Format de date invalide. Utilisez dd/MM/yyyy");
        }

        // Vérifier si le véhicule est déjà loué
        List<Rental> activeRentals = rentalRepository.findActiveRentalsByVehiclePlateNumberAndDate(
                plateNumber, new Date());
        if (!activeRentals.isEmpty()) {
            throw new Exception("Le véhicule est déjà loué");
        }

        // Créer une personne ou la récupérer si elle existe
        Person person = personRepository.findByName(rentalRequest.getPersonName());
        if (person == null) {
            person = new Person(rentalRequest.getPersonName());
            personRepository.save(person);
        }

        // Créer la location
        Rental rental = new Rental(beginDate, endDate);
        rental.setPerson(person);
        rental.setVehicle(vehicle);

        return rentalRepository.save(rental);
    }

    // Retourner un véhicule
    @Transactional
    public void returnVehicle(String plateNumber) throws Exception {
        // Vérifier si le véhicule existe
        vehicleRepository.findById(plateNumber)
                .orElseThrow(() -> new Exception("Véhicule non trouvé: " + plateNumber));

        // Rechercher les locations actives pour ce véhicule
        List<Rental> activeRentals = rentalRepository.findActiveRentalsByVehiclePlateNumberAndDate(
                plateNumber, new Date());

        if (activeRentals.isEmpty()) {
            throw new Exception("Le véhicule n'est pas actuellement loué");
        }

        // Terminer la location en modifiant la date de fin
        Rental activeRental = activeRentals.get(0);
        activeRental.setEndDate(new Date());
        rentalRepository.save(activeRental);
    }
}