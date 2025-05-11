package services;

import models.Apartment;
import models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ApartmentRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;

    @Autowired
    public ApartmentService(ApartmentRepository apartmentRepository) {
        this.apartmentRepository = apartmentRepository;
    }

    // Lấy tất cả căn hộ của một chủ sở hữu
    public List<Apartment> getAllApartmentsByOwner(User owner) {
        return apartmentRepository.findAllByOwner(owner);
    }

    // Lấy căn hộ theo số phòng
    public Apartment getApartmentByRoomNumber(String roomNumber) {
        return apartmentRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RuntimeException("Apartment not found with room number: " + roomNumber));
    }
    public Apartment getApartmentByOwner(User owner) {
        return apartmentRepository.findByOwner(owner);
    }

    // Lấy tất cả căn hộ theo tầng
    public List<Apartment> getApartmentsByFloor(Integer floor) {
        return apartmentRepository.findByFloor(floor);
    }

    // Lấy tất cả căn hộ có diện tích lớn hơn hoặc bằng một giá trị cho trước
    public List<Apartment> getApartmentsByMinArea(Float area) {
        return apartmentRepository.findByAreaGreaterThanEqual(area);
    }

    public List<Apartment> getApartmentsByRoomNumberAndFloor(String roomNumber, Integer floor) {
        if (roomNumber != null && floor != null) {
            return apartmentRepository.findByRoomNumberAndFloor(roomNumber, floor);
        } else if (roomNumber != null) {
            Optional<Apartment> apartment = apartmentRepository.findByRoomNumber(roomNumber);
            return apartment.map(Arrays::asList).orElse(Collections.emptyList());
        } else if (floor != null) {
            return apartmentRepository.findByFloor(floor);
        } else {
            return apartmentRepository.findAll();
        }
    }

    // Lấy tất cả căn hộ theo số phòng ngủ
    public List<Apartment> getApartmentsByBedroomCount(Integer bedroomCount) {
        return apartmentRepository.findByBedroomCount(bedroomCount);
    }

    // Lấy tất cả căn hộ theo số phòng tắm
    public List<Apartment> getApartmentsByBathroomCount(Integer bathroomCount) {
        return apartmentRepository.findByBathroomCount(bathroomCount);
    }

    // Cập nhật thông tin căn hộ
    public Apartment updateApartment(Apartment apartment) {
        if (apartment.getId() == null || !apartmentRepository.existsById(apartment.getId())) {
            throw new RuntimeException("Apartment not found with ID: " + apartment.getId());
        }
        return apartmentRepository.save(apartment);
    }

    // Thêm mới căn hộ
    public Apartment saveApartment(Apartment apartment) {
        if (apartmentRepository.existsById(apartment.getId())) {
            throw new RuntimeException("Apartment with ID " + apartment.getId() + " already exists.");
        }
        return apartmentRepository.save(apartment);
    }

    // Xóa căn hộ theo ID
    public void deleteApartment(Long apartmentId) {
        if (!apartmentRepository.existsById(apartmentId)) {
            throw new RuntimeException("Apartment not found with ID: " + apartmentId);
        }
        apartmentRepository.deleteById(apartmentId);
    }

    // Lấy căn hộ theo ID
    public Apartment getApartmentById(Long id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Apartment not found with ID: " + id));
    }


    // Lấy tất cả căn hộ
    public List<Apartment> getAllApartments() {
        return apartmentRepository.findAll();
    }

    // Tìm tất cả căn hộ theo các điều kiện bổ sung như số lượng xe máy, ô tô, diện tích...
    public List<Apartment> getApartmentsByFilters(Integer motorbikeCount, Integer carCount, Float area, Integer bedroomCount, Integer bathroomCount, Integer floor) {
        if (motorbikeCount != null && carCount != null) {
            return apartmentRepository.findByMotorbikeCountAndCarCount(motorbikeCount, carCount);
        }
        if (area != null && bedroomCount != null) {
            return apartmentRepository.findByAreaGreaterThanEqualAndBedroomCount(area, bedroomCount);
        }
        if (area != null && bathroomCount != null) {
            return apartmentRepository.findByAreaGreaterThanEqualAndBathroomCount(area, bathroomCount);
        }
        if (floor != null) {
            return apartmentRepository.findByFloor(floor);
        }
        return apartmentRepository.findAll();
    }

    // Lấy tất cả căn hộ theo số lượng xe máy
    public List<Apartment> getApartmentsByMotorbikeCount(Integer motorbikeCount) {
        return apartmentRepository.findByMotorbikeCount(motorbikeCount);
    }

    // Lấy tất cả căn hộ theo số lượng ô tô
    public List<Apartment> getApartmentsByCarCount(Integer carCount) {
        return apartmentRepository.findByCarCount(carCount);
    }

    // Tìm căn hộ theo tầng và diện tích
    public List<Apartment> getApartmentsByFloorAndArea(Integer floor, Float minArea) {
        return apartmentRepository.findByFloorAndAreaGreaterThanEqual(floor, minArea);
    }

    // Tìm căn hộ theo số lượng xe máy và ô tô
    public List<Apartment> getApartmentsByMotorbikeAndCarCount(Integer motorbikeCount, Integer carCount) {
        return apartmentRepository.findByMotorbikeCountAndCarCount(motorbikeCount, carCount);
    }

    // Tìm căn hộ theo diện tích và số phòng ngủ
    public List<Apartment> getApartmentsByAreaAndBedroomCount(Float area, Integer bedroomCount) {
        return apartmentRepository.findByAreaGreaterThanEqualAndBedroomCount(area, bedroomCount);
    }

    // Tìm căn hộ theo diện tích và số phòng tắm
    public List<Apartment> getApartmentsByAreaAndBathroomCount(Float area, Integer bathroomCount) {
        return apartmentRepository.findByAreaGreaterThanEqualAndBathroomCount(area, bathroomCount);
    }

    // Tìm căn hộ theo chủ sở hữu và tầng
    public List<Apartment> getApartmentsByOwnerAndFloor(User owner, Integer floor) {
        return apartmentRepository.findByOwnerAndFloor(owner, floor);
    }

    // Tìm căn hộ theo chủ sở hữu và diện tích
    public List<Apartment> getApartmentsByOwnerAndArea(User owner, Float area) {
        return apartmentRepository.findByOwnerAndAreaGreaterThanEqual(owner, area);
    }
}
