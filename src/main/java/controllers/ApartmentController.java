package controllers;

import models.Apartment;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    // Lấy tất cả căn hộ của một chủ sở hữu
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

    // Lấy căn hộ theo số phòng
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

    // Lấy danh sách căn hộ theo các bộ lọc
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

    // Phương thức GET cho /api/apartments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Apartment> getApartmentById(@PathVariable Long id) {
        logger.info("Request received for apartment with ID: {}", id);
        try {
            Apartment apartment = apartmentService.getApartmentById(id);
            if (apartment == null) {
                logger.error("Apartment with ID {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Apartment found: {}", apartment);
            return ResponseEntity.ok(apartment);
        } catch (RuntimeException e) {
            logger.error("Error retrieving apartment with ID {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    // Thêm mới căn hộ
    @PostMapping
    public ResponseEntity<Apartment> createApartment(@RequestBody Apartment apartment) {
        logger.info("Received request to create new apartment: {}", apartment);

        // Kiểm tra xem thông tin căn hộ có hợp lệ không
        if (apartment == null || apartment.getRoomNumber() == null || apartment.getOwner() == null) {
            logger.error("Invalid apartment data received");
            return ResponseEntity.badRequest().build();
        }

        Apartment createdApartment = apartmentService.saveApartment(apartment);
        logger.info("Apartment created successfully with ID: {}", createdApartment.getId());

        return ResponseEntity.status(201).body(createdApartment);
    }

    // Cập nhật thông tin căn hộ
    @PutMapping("/{id}")
    public ResponseEntity<Apartment> updateApartment(@PathVariable Long id, @RequestBody Apartment apartment) {
        logger.info("Received request to update apartment with ID: {}", id);

        // Kiểm tra xem căn hộ có tồn tại trong cơ sở dữ liệu hay không
        Apartment existingApartment = apartmentService.getApartmentById(id);
        if (existingApartment == null) {
            logger.error("Apartment with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }

        // Cập nhật thông tin căn hộ
        existingApartment.setRoomNumber(apartment.getRoomNumber());
        existingApartment.setFloor(apartment.getFloor());
        existingApartment.setArea(apartment.getArea());
        existingApartment.setMotorbikeCount(apartment.getMotorbikeCount());
        existingApartment.setCarCount(apartment.getCarCount());
        existingApartment.setBedroomCount(apartment.getBedroomCount());
        existingApartment.setBathroomCount(apartment.getBathroomCount());
        existingApartment.setOwner(apartment.getOwner());

        Apartment updatedApartment = apartmentService.updateApartment(existingApartment);
        logger.info("Apartment with ID {} updated successfully", updatedApartment.getId());

        return ResponseEntity.ok(updatedApartment);
    }

    // Xóa căn hộ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApartment(@PathVariable Long id) {
        logger.info("Received request to delete apartment with ID: {}", id);

        // Kiểm tra xem căn hộ có tồn tại không
        Apartment apartment = apartmentService.getApartmentById(id);
        if (apartment == null) {
            logger.error("Apartment with ID {} not found", id);
            return ResponseEntity.notFound().build();
        }

        // Xóa căn hộ
        apartmentService.deleteApartment(id);
        logger.info("Apartment with ID {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }
}
