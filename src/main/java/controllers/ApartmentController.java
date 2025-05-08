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

import java.util.ArrayList;
import java.util.List;

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
    public ResponseEntity<List<Apartment>> getApartmentsByFilters(
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Float area,
            @RequestParam(required = false) Integer bedroomCount,
            @RequestParam(required = false) Integer bathroomCount) {

        logger.info("Received request to get apartments with filters - Room: {}, Floor: {}, Area: {}, Bedrooms: {}, Bathrooms: {}",
                roomNumber, floor, area, bedroomCount, bathroomCount);

        List<Apartment> apartments = new ArrayList<>();

        // Kiểm tra và xử lý roomNumber nếu có
        if (roomNumber != null) {
            Apartment apartment = apartmentService.getApartmentByRoomNumber(roomNumber);
            if (apartment != null) {
                apartments.add(apartment);
                logger.info("Found apartment with room number: {}", roomNumber);
            } else {
                logger.error("No apartment found for room number: {}", roomNumber);
            }
        }

        // Kiểm tra và xử lý floor nếu có
        if (floor != null) {
            List<Apartment> apartmentsByFloor = apartmentService.getApartmentsByFloor(floor);
            apartments.addAll(apartmentsByFloor);
            logger.info("Found {} apartments on floor: {}", apartmentsByFloor.size(), floor);
        }

        // Kiểm tra và xử lý area nếu có
        if (area != null) {
            List<Apartment> apartmentsByArea = apartmentService.getApartmentsByMinArea(area);
            apartments.addAll(apartmentsByArea);
            logger.info("Found {} apartments with minimum area {}", apartmentsByArea.size(), area);
        }

        // Kiểm tra và xử lý bedroomCount nếu có
        if (bedroomCount != null) {
            List<Apartment> apartmentsByBedrooms = apartmentService.getApartmentsByBedroomCount(bedroomCount);
            apartments.addAll(apartmentsByBedrooms);
            logger.info("Found {} apartments with bedroom count {}", apartmentsByBedrooms.size(), bedroomCount);
        }

        // Kiểm tra và xử lý bathroomCount nếu có
        if (bathroomCount != null) {
            List<Apartment> apartmentsByBathrooms = apartmentService.getApartmentsByBathroomCount(bathroomCount);
            apartments.addAll(apartmentsByBathrooms);
            logger.info("Found {} apartments with bathroom count {}", apartmentsByBathrooms.size(), bathroomCount);
        }

        // Nếu không có bộ lọc nào được chỉ định, trả về tất cả căn hộ
        if (apartments.isEmpty()) {
            apartments = apartmentService.getAllApartments();
            logger.info("Returning all apartments");
        }

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
