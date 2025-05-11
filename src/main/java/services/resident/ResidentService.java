package services.resident;

import jakarta.persistence.EntityNotFoundException;
import models.resident.Resident;
import models.user.User;
import models.enums.*;
import org.springframework.stereotype.Service;
import repositories.resident.ResidentRepository;
import repositories.user.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ResidentService {
    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;

    // Constructor Injection
    public ResidentService(ResidentRepository residentRepository, UserRepository userRepository) {
        this.residentRepository = residentRepository;
        this.userRepository = userRepository;
        System.out.println("ResidentService initialized with ResidentRepository and UserRepository.");
    }

    // Kiểm tra và tạo mới Resident nếu chưa có
    public Resident checkAndCreateResidentIfNotExists(Long userId) {
        System.out.println("Kiểm tra và tạo mới Resident nếu chưa có, userId: " + userId);

        // Tìm kiếm User theo ID
        User user = userRepository.findById(userId).orElseThrow(() -> {
            System.out.println("Không tìm thấy User với ID: " + userId);
            return new EntityNotFoundException("User not found");
        });

        System.out.println("Tìm thấy User với ID: " + userId);

        // Kiểm tra xem Resident đã tồn tại trong bảng nhan_khau chưa (dựa trên User)
        Resident resident = residentRepository.findByUser(user);
        if (resident == null) {
            System.out.println("Không tìm thấy Resident cho User, tạo mới Resident...");

            // Nếu chưa có, tạo mới Resident với các trạng thái mặc định
            resident = new Resident();
            resident.setUser(user);
            resident.setTrangThaiXacThuc(XacThuc.CHUA_XAC_THUC); // Trạng thái xác thực là chưa xác thực
            resident.setTrangThaiTamVang(null); // Trạng thái tạm vắng mặc định là null
            resident.setSoPhong(null); // SoPhong mặc định là null
            // Các thông tin khác có thể để mặc định hoặc null

            // Lưu vào cơ sở dữ liệu
            resident = residentRepository.save(resident);
            System.out.println("Resident mới đã được tạo và lưu vào cơ sở dữ liệu.");
        } else {
            System.out.println("Resident đã tồn tại cho User ID: " + userId);
        }

        return resident;
    }

    // Tạo mới một Resident
    public Resident createResident(String hoTen,
                                   String cccd,
                                   String sdt,
                                   LocalDate ngaySinh,
                                   Gender gioiTinh,
                                   TamVangStatus trangThaiTamVang,
                                   XacThuc trangThaiXacThuc,
                                   SoPhong soPhong,
                                   Long userId) {

        System.out.println("Tạo mới Resident, userId: " + userId);

        // Kiểm tra xem User đã tồn tại chưa
        User user = userRepository.findById(userId).orElseThrow(() -> {
            System.out.println("Không tìm thấy User với ID: " + userId);
            return new EntityNotFoundException("User not found");
        });

        // Kiểm tra xem cccd có tồn tại trong hệ thống chưa
        if (residentRepository.findByCccd(cccd) != null) {
            System.out.println("CCCD đã tồn tại trong hệ thống: " + cccd);
            throw new IllegalArgumentException("CCCD already exists");
        }

        // Tạo mới Resident và lưu vào database
        Resident resident = new Resident(hoTen, cccd, sdt, ngaySinh, gioiTinh, trangThaiTamVang, trangThaiXacThuc, soPhong, user);
        Resident savedResident = residentRepository.save(resident);
        System.out.println("Resident mới đã được tạo và lưu vào cơ sở dữ liệu với ID: " + savedResident.getId());

        return savedResident;
    }

    // Cập nhật thông tin Resident
    public Resident updateResident(Long residentId,
                                   String hoTen,
                                   String cccd,
                                   String sdt,
                                   LocalDate ngaySinh,
                                   Gender gioiTinh,
                                   TamVangStatus trangThaiTamVang,
                                   XacThuc trangThaiXacThuc,
                                   SoPhong soPhong) {

        System.out.println("Cập nhật Resident, residentId: " + residentId);

        // Tìm kiếm Resident từ ID
        Resident resident = residentRepository.findById(residentId).orElseThrow(() -> {
            System.out.println("Không tìm thấy Resident với ID: " + residentId);
            return new EntityNotFoundException("Resident not found");
        });

        // Cập nhật các trường thông tin
        resident.setHoTen(hoTen);
        resident.setCccd(cccd);
        resident.setSdt(sdt);
        resident.setNgaySinh(ngaySinh);
        resident.setGioiTinh(gioiTinh);
        resident.setTrangThaiTamVang(trangThaiTamVang);
        resident.setTrangThaiXacThuc(trangThaiXacThuc);
        resident.setSoPhong(soPhong);

        // Lưu lại đối tượng Resident đã được cập nhật
        Resident updatedResident = residentRepository.save(resident);
        System.out.println("Resident đã được cập nhật thành công với ID: " + updatedResident.getId());

        return updatedResident;
    }

    // Xóa Resident theo ID
    public void deleteResident(Long residentId) {
        System.out.println("Xóa Resident, residentId: " + residentId);

        if (!residentRepository.existsById(residentId)) {
            System.out.println("Không tìm thấy Resident với ID: " + residentId);
            throw new EntityNotFoundException("Resident not found with id " + residentId);
        }

        residentRepository.deleteById(residentId);
        System.out.println("Đã xóa Resident với ID: " + residentId);
    }

    //  tất cả Resident
    public List<Resident> getAllResidents() {
        System.out.println("Lấy tất cả Resident...");
        List<Resident> residents = residentRepository.findAll();
        System.out.println("Số lượng Resident tìm thấy: " + residents.size());
        return residents;
    }

    // Tìm kiếm Resident theo ID
    public Resident getResidentById(Long residentId) {
        System.out.println("Tìm kiếm Resident theo ID: " + residentId);

        return residentRepository.findById(residentId)
                .orElseThrow(() -> {
                    System.out.println("Không tìm thấy Resident với ID: " + residentId);
                    return new EntityNotFoundException("Resident not found with id " + residentId);
                });
    }

    public List<Resident> findResidents(SoPhong soPhong, XacThuc xacThuc, TamVangStatus tamVangStatus) {
        return residentRepository.findByFilters(soPhong, xacThuc, tamVangStatus);
    }



    // Tìm kiếm Resident theo User ID
    public Resident findResidentByUserId(Long userId) {
        System.out.println("Tìm kiếm Resident theo User ID: " + userId);

        // Tìm kiếm User theo ID
        User user = userRepository.findById(userId).orElseThrow(() -> {
            System.out.println("Không tìm thấy User với ID: " + userId);
            return new EntityNotFoundException("User not found with id " + userId);
        });

        // Tìm kiếm Resident theo User
        Resident resident = residentRepository.findByUser(user);
        if (resident == null) {
            System.out.println("Không tìm thấy Resident cho User với ID: " + userId);
            throw new EntityNotFoundException("Resident not found for user with id " + userId);
        }

        System.out.println("Tìm thấy Resident cho User với ID: " + userId);
        return resident;
    }

    // Tìm kiếm Resident theo phòng
    public List<Resident> getResidentsByRoom(SoPhong soPhong) {
        System.out.println("Tìm kiếm Resident theo phòng: " + soPhong);
        List<Resident> residents = residentRepository.findBySoPhong(soPhong);
        System.out.println("Số lượng Resident tìm thấy: " + residents.size());
        return residents;
    }

    // Tìm kiếm Resident theo trạng thái tạm vắng
    public List<Resident> getResidentsByStatus(TamVangStatus trangThaiTamVang) {
        System.out.println("Tìm kiếm Resident theo trạng thái tạm vắng: " + trangThaiTamVang);
        List<Resident> residents = residentRepository.findByTrangThaiTamVang(trangThaiTamVang);
        System.out.println("Số lượng Resident tìm thấy: " + residents.size());
        return residents;
    }

    // Tìm kiếm Resident theo CCCD
    public Resident getResidentByCccd(String cccd) {
        System.out.println("Tìm kiếm Resident theo CCCD: " + cccd);

        Resident resident = residentRepository.findByCccd(cccd);
        if (resident == null) {
            System.out.println("Không tìm thấy Resident với CCCD: " + cccd);
            throw new EntityNotFoundException("Resident not found with CCCD " + cccd);
        }

        System.out.println("Tìm thấy Resident với CCCD: " + cccd);
        return resident;
    }

    public Resident findResidentByUsername(String username) {
        System.out.println("Tìm kiếm Resident theo Username: " + username);

        // Tìm kiếm User theo username
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            System.out.println("Không tìm thấy User với Username: " + username);
            return null; // Trả về null nếu không tìm thấy User
        }

        // Tìm kiếm Resident theo User
        Resident resident = residentRepository.findByUser(user);
        if (resident == null) {
            System.out.println("Không tìm thấy Resident cho User với Username: " + username);
            return null; // Trả về null nếu không tìm thấy Resident
        }

        System.out.println("Tìm thấy Resident cho User với Username: " + username);
        return resident;
    }



    public Resident saveResident(Resident resident) {
        System.out.println("Bắt đầu lưu resident. ID: " + resident.getId());
        System.out.println("Trạng thái xác thực: " + resident.getTrangThaiXacThuc());

        if (resident.getId() != null && residentRepository.existsById(resident.getId())) {
            System.out.println("Resident đã tồn tại. Tiến hành cập nhật...");

            return updateResident(resident.getId(),
                    resident.getHoTen(),
                    resident.getCccd(),
                    resident.getSdt(),
                    resident.getNgaySinh(),
                    resident.getGioiTinh(),
                    resident.getTrangThaiTamVang(),
                    resident.getTrangThaiXacThuc(),
                    resident.getSoPhong());
        } else {
            System.out.println("Resident mới. Tiến hành tạo mới...");

            return residentRepository.save(resident);
        }
    }


}