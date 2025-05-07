package controllers;

import models.Apartment;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.ApartmentService;
import services.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/apartments")
public class ApartmentController {

    private final ApartmentService apartmentService;
    private final UserService userService;

    // Khởi tạo logger để ghi log
    private static final Logger logger = LoggerFactory.getLogger(ApartmentController.class);

    @Autowired
    public ApartmentController(ApartmentService apartmentService, UserService userService) {
        this.apartmentService = apartmentService;
        this.userService = userService;
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Apartment>> getAllApartmentsByOwner(@PathVariable Long ownerId) {
        logger.info("Received request to get apartments for owner with ID: {}", ownerId);

        User owner = userService.getUserById(ownerId);
        if (owner == null) {
            logger.error("Owner with ID {} not found", ownerId);
            return ResponseEntity.notFound().build();
        }

        List<Apartment> apartments = apartmentService.getAllApartmentsByOwner(owner);
        logger.info("Found {} apartments for owner with ID {}", apartments.size(), ownerId);

        return ResponseEntity.ok(apartments);
    }

    @GetMapping("/room")
    public ResponseEntity<Apartment> getApartmentByRoomNumber(@RequestParam String roomNumber) {
        logger.info("Received request to get apartment for room number: {}", roomNumber);

        Apartment apartment = apartmentService.getApartmentByRoomNumber(roomNumber);
        if (apartment == null) {
            logger.error("No apartment found for room number: {}", roomNumber);
            return ResponseEntity.notFound().build();
        }

        logger.info("Apartment found for room number: {}", roomNumber);
        return ResponseEntity.ok(apartment);
    }


    @GetMapping
    public ResponseEntity<List<Apartment>> getApartmentsByFloor(@RequestParam int floor) {
        logger.info("Received request to get apartments for floor: {}", floor);

        List<Apartment> apartments = apartmentService.getApartmentsByFloor(floor);
        logger.info("Found {} apartments on floor {}", apartments.size(), floor);

        return ResponseEntity.ok(apartments);
    }


    @GetMapping("/area/{area}")
    public ResponseEntity<List<Apartment>> getApartmentsByMinArea(@PathVariable float area) {
        logger.info("Received request to get apartments with minimum area: {}", area);

        List<Apartment> apartments = apartmentService.getApartmentsByMinArea(area);
        logger.info("Found {} apartments with minimum area {}", apartments.size(), area);

        return ResponseEntity.ok(apartments);
    }

    @GetMapping("/bedrooms/{bedroomCount}")
    public ResponseEntity<List<Apartment>> getApartmentsByBedroomCount(@PathVariable int bedroomCount) {
        logger.info("Received request to get apartments with bedroom count: {}", bedroomCount);

        List<Apartment> apartments = apartmentService.getApartmentsByBedroomCount(bedroomCount);
        logger.info("Found {} apartments with bedroom count {}", apartments.size(), bedroomCount);

        return ResponseEntity.ok(apartments);
    }

    @GetMapping("/bathrooms/{bathroomCount}")
    public ResponseEntity<List<Apartment>> getApartmentsByBathroomCount(@PathVariable int bathroomCount) {
        logger.info("Received request to get apartments with bathroom count: {}", bathroomCount);

        List<Apartment> apartments = apartmentService.getApartmentsByBathroomCount(bathroomCount);
        logger.info("Found {} apartments with bathroom count {}", apartments.size(), bathroomCount);

        return ResponseEntity.ok(apartments);
    }
}
