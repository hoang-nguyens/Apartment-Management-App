package models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "apartments")
public class Apartment extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Tầng không được để trống")
    @Column(nullable = false)
    private Integer floor;

    @NotNull(message = "Số phòng không được để trống")
    @Size(max = 20, message = "Số phòng tối đa 20 ký tự")
    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @NotNull(message = "Chủ sở hữu không được để trống")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @NotNull(message = "Diện tích không được để trống")
    @Positive(message = "Diện tích phải lớn hơn 0")
    @Column(nullable = false)
    private Float area;

    @PositiveOrZero(message = "Số xe máy không được âm")
    @Column(name = "motorbike_count", nullable = false)
    private Integer motorbikeCount = 0;

    @PositiveOrZero(message = "Số ô tô không được âm")
    @Column(name = "car_count", nullable = false)
    private Integer carCount = 0;

    @PositiveOrZero(message = "Số phòng ngủ không được âm")
    @Column(name = "bedroom_count", nullable = false)
    private Integer bedroomCount = 0;

    @PositiveOrZero(message = "Số phòng tắm không được âm")
    @Column(name = "bathroom_count", nullable = false)
    private Integer bathroomCount = 0;

    // ===== Getter & Setter =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Float getArea() {
        return area;
    }

    public void setArea(Float area) {
        this.area = area;
    }

    public Integer getMotorbikeCount() {
        return motorbikeCount;
    }

    public void setMotorbikeCount(Integer motorbikeCount) {
        this.motorbikeCount = motorbikeCount;
    }

    public Integer getCarCount() {
        return carCount;
    }

    public void setCarCount(Integer carCount) {
        this.carCount = carCount;
    }

    public Integer getBedroomCount() {
        return bedroomCount;
    }

    public void setBedroomCount(Integer bedroomCount) {
        this.bedroomCount = bedroomCount;
    }

    public Integer getBathroomCount() {
        return bathroomCount;
    }

    public void setBathroomCount(Integer bathroomCount) {
        this.bathroomCount = bathroomCount;
    }
}
