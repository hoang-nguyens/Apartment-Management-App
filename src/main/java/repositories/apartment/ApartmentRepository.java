package repositories.apartment;

import models.apartment.Apartment;
import models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    // Tìm các căn hộ của một chủ sở hữu
    List<Apartment> findAllByOwner(User owner);

    Apartment findByOwner(User user);
    // Tìm căn hộ theo số phòng
    Optional<Apartment> findByRoomNumber(String roomNumber);

    // Tìm căn hộ theo tầng
    List<Apartment> findByFloor(Integer floor);

    // Tìm căn hộ theo số phòng và tầng
    List<Apartment> findByRoomNumberAndFloor(String roomNumber, Integer floor);

    // Tìm căn hộ có diện tích lớn hơn hoặc bằng một giá trị nhất định
    List<Apartment> findByAreaGreaterThanEqual(Float area);

    // Tìm căn hộ theo số phòng ngủ
    List<Apartment> findByBedroomCount(Integer bedroomCount);

    // Tìm căn hộ theo số phòng tắm
    List<Apartment> findByBathroomCount(Integer bathroomCount);

    // Tìm căn hộ theo số lượng xe máy
    List<Apartment> findByMotorbikeCount(Integer motorbikeCount);

    // Tìm căn hộ theo số lượng ô tô
    List<Apartment> findByCarCount(Integer carCount);

    // Tìm căn hộ theo tầng và diện tích
    List<Apartment> findByFloorAndAreaGreaterThanEqual(Integer floor, Float area);

    // Tìm căn hộ theo tầng và số phòng ngủ
    List<Apartment> findByFloorAndBedroomCount(Integer floor, Integer bedroomCount);

    // Tìm căn hộ theo tầng và số phòng tắm
    List<Apartment> findByFloorAndBathroomCount(Integer floor, Integer bathroomCount);

    // Tìm căn hộ theo số lượng xe máy và ô tô
    List<Apartment> findByMotorbikeCountAndCarCount(Integer motorbikeCount, Integer carCount);

    // Tìm căn hộ theo diện tích và số phòng ngủ
    List<Apartment> findByAreaGreaterThanEqualAndBedroomCount(Float area, Integer bedroomCount);

    // Tìm căn hộ theo diện tích và số phòng tắm
    List<Apartment> findByAreaGreaterThanEqualAndBathroomCount(Float area, Integer bathroomCount);

    // Lấy tất cả căn hộ
    List<Apartment> findAll();

    // Tìm căn hộ theo chủ sở hữu và tầng
    List<Apartment> findByOwnerAndFloor(User owner, Integer floor);

    // Tìm căn hộ theo chủ sở hữu và diện tích
    List<Apartment> findByOwnerAndAreaGreaterThanEqual(User owner, Float area);

    // Tìm căn hộ theo ID
//    Optional<Apartment> findById(Long id);

}
