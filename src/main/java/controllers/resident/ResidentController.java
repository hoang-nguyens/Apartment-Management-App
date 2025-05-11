package controllers.resident;

import models.resident.Resident;
import models.enums.Gender;
import models.enums.SoPhong;
import models.enums.TamVangStatus;
import models.enums.XacThuc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.resident.ResidentService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/residents")
public class ResidentController {

    private final ResidentService residentService;

    @Autowired
    public ResidentController(ResidentService residentService) {
        this.residentService = residentService;
    }

    @GetMapping
    public ResponseEntity<List<Resident>> getResidents(
            @RequestParam(value = "so_phong_id", required = false) String room,
            @RequestParam(value = "trang_thai_xac_thuc", required = false) String authStatus,
            @RequestParam(value = "trang_thai_tam_vang", required = false) String tamVangStatus) {

        System.out.println("== Đã vào API /api/residents ==");
        System.out.println("Room: " + room);
        System.out.println("Auth Status: " + authStatus);
        System.out.println("Tam Vang Status: " + tamVangStatus);

        if (room == null && authStatus == null && tamVangStatus == null) {
            List<Resident> allResidents = residentService.getAllResidents();
            return ResponseEntity.ok(allResidents);
        }

        SoPhong soPhong = toEnum(SoPhong.class, room);
        XacThuc xacThuc = toEnum(XacThuc.class, authStatus);
        TamVangStatus tamVang = toEnumTamVangStatus(tamVangStatus);

        List<Resident> result = residentService.findResidents(soPhong, xacThuc, tamVang);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resident> getResidentById(@PathVariable Long id) {
        Resident resident = residentService.getResidentById(id);
        return ResponseEntity.ok(resident);
    }

    @PostMapping
    public ResponseEntity<?> createResident(@RequestBody Map<String, Object> requestBody) {
        try {
            String hoTen = (String) requestBody.get("hoTen");
            String cccd = (String) requestBody.get("cccd");
            String sdt = (String) requestBody.get("sdt");

            List<Integer> ngaySinhList = (List<Integer>) requestBody.get("ngaySinh");
            LocalDate ngaySinh = LocalDate.of(ngaySinhList.get(0), ngaySinhList.get(1), ngaySinhList.get(2));

            Gender gioiTinh = Gender.valueOf((String) requestBody.get("gioiTinh"));
            TamVangStatus trangThaiTamVang = TamVangStatus.valueOf((String) requestBody.get("trangThaiTamVang"));
            XacThuc trangThaiXacThuc = XacThuc.valueOf((String) requestBody.get("trangThaiXacThuc"));
            SoPhong soPhong = SoPhong.valueOf((String) requestBody.get("soPhong"));

            Long userId = Long.parseLong(requestBody.get("userId").toString());

            Resident createdResident = residentService.createResident(
                    hoTen, cccd, sdt, ngaySinh, gioiTinh, trangThaiTamVang, trangThaiXacThuc, soPhong, userId
            );

            return ResponseEntity.ok(createdResident);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xử lý yêu cầu: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResident(@PathVariable Long id, @RequestBody Map<String, Object> requestBody) {
        try {
            String hoTen = (String) requestBody.get("hoTen");
            String cccd = (String) requestBody.get("cccd");
            String sdt = (String) requestBody.get("sdt");

            List<Integer> ngaySinhList = (List<Integer>) requestBody.get("ngaySinh");
            LocalDate ngaySinh = LocalDate.of(ngaySinhList.get(0), ngaySinhList.get(1), ngaySinhList.get(2));

            Gender gioiTinh = Gender.valueOf((String) requestBody.get("gioiTinh"));
            TamVangStatus trangThaiTamVang = TamVangStatus.valueOf((String) requestBody.get("trangThaiTamVang"));
            XacThuc trangThaiXacThuc = XacThuc.valueOf((String) requestBody.get("trangThaiXacThuc"));
            SoPhong soPhong = SoPhong.valueOf((String) requestBody.get("soPhong"));

            Resident updatedResident = residentService.updateResident(
                    id, hoTen, cccd, sdt, ngaySinh, gioiTinh, trangThaiTamVang, trangThaiXacThuc, soPhong
            );

            return ResponseEntity.ok(updatedResident);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xử lý yêu cầu: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteResident(@PathVariable Long id) {
        try {
            residentService.deleteResident(id);
            return ResponseEntity.ok("Đã xóa thành công Resident với id " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/cccd/{cccd}")
    public ResponseEntity<Resident> getResidentByCccd(@PathVariable String cccd) {
        try {
            Resident resident = residentService.getResidentByCccd(cccd);
            return ResponseEntity.ok(resident);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Resident>> getResidentsByRoom(@PathVariable String roomId) {
        try {
            SoPhong soPhongEnum = SoPhong.valueOf(roomId);
            List<Resident> residents = residentService.getResidentsByRoom(soPhongEnum);
            return ResponseEntity.ok(residents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Resident>> getResidentsByStatus(@PathVariable String status) {
        try {
            TamVangStatus trangThaiTamVangEnum = TamVangStatus.valueOf(status);
            List<Resident> residents = residentService.getResidentsByStatus(trangThaiTamVangEnum);
            return ResponseEntity.ok(residents);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Chuyển String thành enum (dùng cho SoPhong, XacThuc)
    private <T extends Enum<T>> T toEnum(Class<T> enumClass, String value) {
        try {
            if (value != null) {
                value = value.replace(" ", "_").toUpperCase();
            }
            return value != null ? Enum.valueOf(enumClass, value) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Chuyển String thành enum TamVangStatus (chữ thường)
    private TamVangStatus toEnumTamVangStatus(String value) {
        try {
            if (value != null) {
                value = value.replace(" ", "_").toLowerCase();
            }
            return value != null ? TamVangStatus.valueOf(value) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}