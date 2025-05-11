package controllers.resident;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import javafx.stage.Stage;
import models.resident.Resident;
import models.user.User;
import models.enums.*;
import org.springframework.stereotype.Controller;
import services.resident.ResidentService;
import utils.UserUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

@Controller
public class ResidentEditController{
    private Stage stage;
    private Resident resident;
    private final ResidentService residentService;
    public ResidentEditController(ResidentService residentService) {
        this.residentService = residentService;
    }
    /*
    Các ô cần thiết cho pop up này
    Hiện username (nhưng không được thay đổi gì)
    Nhập họ tên
    Nhập số CCCD
    Nhập số điện thoại
    Nhập ngày tháng năm sinh
    Nhập giới tính (đây là một ô lựa chọn)
    Nhập trạng thái tạm vắng (cũng  là một ô lựa chọn)
    Nhập số phòng (Chỉ nhập các phòng đã bị giới hạn trong phần model.enums
    Nhập mô tả thêm gì đó
     */
    @FXML
    private TextField nameField;
    @FXML
    private TextField usenameField;
    @FXML
    private TextField cccdField;
    @FXML
    private TextField stdField;
    @FXML
    private DatePicker ngaysinhField;
    @FXML
    private ComboBox<Gender> gioiTinhField;
    @FXML
    private ComboBox<TamVangStatus> trangThaiTamVangField;
    @FXML
    private ComboBox<SoPhong> soPhongField;
    @FXML
    private ComboBox<XacThuc> trangThaiXacThucField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancleButton;

    private final HttpClient hhtpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @FXML
    public void initialize() {
        User currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Không tìm thấy người dùng hiện tại.");
            return;
        }

        Role role = currentUser.getRole();
        if(role == Role.USER){
            resident = residentService.findResidentByUserId(currentUser.getId());
            setResident(resident,1);
            return;
        }
        gioiTinhField.getItems().addAll(Gender.values());
        gioiTinhField.setPromptText("");
        trangThaiTamVangField.getItems().addAll(TamVangStatus.values());
        trangThaiTamVangField.setPromptText("");
        soPhongField.getItems().addAll(SoPhong.values());
        soPhongField.setPromptText("");
        trangThaiXacThucField.getItems().addAll(XacThuc.values());
        trangThaiXacThucField.setPromptText("");

        // Prompt text cho các trường nhập liệu
        nameField.setPromptText("Nhập họ tên");
        usenameField.setPromptText("Tên đăng nhập");
        cccdField.setPromptText("Mã số CCCD");
        stdField.setPromptText("Số điện thoại");
        ngaysinhField.setPromptText("mm/dd/yy");
        cccdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                cccdField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (cccdField.getText().length() > 12) {
                cccdField.setText(cccdField.getText(0, 12));
            }
        });
        stdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                stdField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (stdField.getText().length() > 11) {
                stdField.setText(stdField.getText(0, 11));
            }
        });
        statusLabel.setText("");
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }


    public void setResident(Resident resident, int mode) {
        this.resident = resident;
        if (resident != null) {
            populateForm(resident);
            if (resident.getGioiTinh() != null) {
                gioiTinhField.setValue(resident.getGioiTinh());
            }
            if (resident.getTrangThaiTamVang() != null) {
                trangThaiTamVangField.setValue(resident.getTrangThaiTamVang());
            }
            if (resident.getSoPhong() != null) {
                soPhongField.setValue(resident.getSoPhong());
            }
            if (resident.getTrangThaiXacThuc() != null) {
                trangThaiXacThucField.setValue(resident.getTrangThaiXacThuc());
            }
        }
        usenameField.setEditable(true);
        if (mode == 1) {
            nameField.setEditable(false);
            usenameField.setEditable(false);
            cccdField.setEditable(false);
            stdField.setEditable(false);
            ngaysinhField.setEditable(false);
            gioiTinhField.setEditable(false);
            trangThaiTamVangField.setEditable(false);
            trangThaiXacThucField.setEditable(false);
            soPhongField.setEditable(false);
            saveButton.setVisible(false);
            cancleButton.setVisible(false);
        }
    }
    public Resident getResident() {return resident;}
    private void populateForm(Resident resident) {
        if (resident == null) {
            System.err.println("Resident is null!");
            return;
        }
        try {
            System.out.println(">>> Bắt đầu populateForm");
            System.out.println("HoTen: " + resident.getHoTen());
            System.out.println("CCCD: " + resident.getCccd());
            System.out.println("SDT: " + resident.getSdt());
            System.out.println("NgaySinh: " + resident.getNgaySinh());
            System.out.println("GioiTinh: " + resident.getGioiTinh());
            System.out.println("TrangThaiTamVang: " + resident.getTrangThaiTamVang());
            System.out.println("SoPhong: " + resident.getSoPhong());
            System.out.println("TrangThaiXacThuc: " + resident.getTrangThaiXacThuc());
            System.out.println("User: " + resident.getUser());
            nameField.setText(safeString(resident.getHoTen()));
            cccdField.setText(safeString(resident.getCccd()));
            stdField.setText(safeString(resident.getSdt()));
            ngaysinhField.setValue(resident.getNgaySinh());

            // Điền thông tin User nếu có
            if (resident.getUser() != null) {
                System.out.println("Username: " + resident.getUser().getUsername());
                usenameField.setText(safeString(resident.getUser().getUsername()));
            } else {
                System.err.println("User is null for resident: " + resident.getHoTen());
                usenameField.setText(""); // hoặc giữ nguyên nếu cần
            }

            // Các trường enum (giới tính, trạng thái, phòng)
            if (resident.getGioiTinh() != null) {
                gioiTinhField.setValue(resident.getGioiTinh());
            }

            if (resident.getTrangThaiTamVang() != null) {
                trangThaiTamVangField.setValue(resident.getTrangThaiTamVang());
            }

            if (resident.getSoPhong() != null) {
                soPhongField.setValue(resident.getSoPhong());
            }

            if (resident.getTrangThaiXacThuc() != null) {
                trangThaiXacThucField.setValue(resident.getTrangThaiXacThuc());
            }

            System.out.println(">>> Kết thúc populateForm");

        } catch (Exception e) {
            System.err.println("Lỗi khi populateForm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Hàm phụ trợ tránh lỗi null cho String
    private String safeString(String value) {
        return value != null ? value : "";
    }


    @FXML
    private void handleSave() {
        try {
            if (!validateResidentInput()) {
                statusLabel.setText("Vui lòng điền đầy đủ thông tin hợp lệ.");
                return;
            }
            boolean inserted = false;
            if (resident == null) {
                resident = new Resident();
                resident.setUser(new User());
                inserted = true;
            }
            resident.setHoTen(nameField.getText());
            resident.getUser().setUsername(usenameField.getText());
            resident.setCccd(cccdField.getText());
            resident.setSdt(stdField.getText());
            resident.setNgaySinh(ngaysinhField.getValue());
            resident.setGioiTinh(gioiTinhField.getValue());
            resident.setTrangThaiTamVang(trangThaiTamVangField.getValue());
            resident.setTrangThaiXacThuc(trangThaiXacThucField.getValue());
            resident.setSoPhong(soPhongField.getValue());
            if (resident.getHoTen() == null || resident.getHoTen().isEmpty() ||
                    resident.getUser().getUsername() == null || resident.getUser().getUsername().isEmpty() ||
                    resident.getCccd() == null || resident.getCccd().isEmpty()) {
                statusLabel.setText("Thông tin không hợp lệ! Vui lòng kiểm tra lại.");
                return;
            }
            System.out.println("Resident data before request: ");
            System.out.println("HoTen: " + resident.getHoTen());
            System.out.println("Username: " + resident.getUser().getUsername());
            System.out.println("CCCD: " + resident.getCccd());
            System.out.println("Sdt: " + resident.getSdt());
            System.out.println("NgaySinh: " + resident.getNgaySinh());
            System.out.println("GioiTinh: " + resident.getGioiTinh());
            System.out.println("TrangThaiTamVang: " + resident.getTrangThaiTamVang());
            System.out.println("TrangThaiXacThuc: " + resident.getTrangThaiXacThuc());
            System.out.println("SoPhong: " + resident.getSoPhong());
            System.out.println("User: " + (resident.getUser() != null ? resident.getUser().getUsername() : "No user"));
            String requestBody = objectMapper.writeValueAsString(resident);
            System.out.println("Request Body (JSON): " + requestBody); // In ra requestBody để kiểm tra
            HttpRequest request;
            if (inserted) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/residents"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
            } else {
                if (resident.getId() == null || resident.getId() <= 0) {
                    statusLabel.setText("ID không hợp lệ.");
                    return;
                }
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/residents/" + resident.getId()))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
            }

            HttpResponse<String> response = hhtpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response status code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
            if (response.statusCode() == 200) {
                resident = objectMapper.readValue(response.body(), Resident.class);
                statusLabel.setText("Thao tác thành công!");
                stage.close();
            } else {
                statusLabel.setText("Lỗi request: " + response.body());
                System.out.println("Chi tiết lỗi: " + response.body());
            }

        } catch (IllegalArgumentException e) {
            statusLabel.setText("Lỗi nhập liệu: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // để dễ debug hơn
            statusLabel.setText("Lỗi: " + e.getMessage());
        }
    }



    private boolean validateResidentInput() {
        try {
            if (isNullOrEmpty(nameField.getText())) {
                statusLabel.setText("Họ tên không được để trống!");
                return false;
            }
            if (isNullOrEmpty(usenameField.getText())) {
                statusLabel.setText("Username không được để trống!");
                return false;
            }
            String cccd = cccdField.getText();
            if (isNullOrEmpty(cccd)) {
                statusLabel.setText("CCCD không được để trống!");
                return false;
            }
            if (!cccd.matches("\\d{12}")) {
                statusLabel.setText("CCCD phải bao gồm đúng 12 chữ số!");
                return false;
            }
            String sdt = stdField.getText();
            if (isNullOrEmpty(sdt)) {
                statusLabel.setText("Số điện thoại không được để trống!");
                return false;
            }
            if (!sdt.matches("\\d{10,11}")) {
                statusLabel.setText("Số điện thoại phải bao gồm 10–11 chữ số!");
                return false;
            }
            LocalDate ngaySinh = ngaysinhField.getValue();
            if (ngaySinh == null) {
                statusLabel.setText("Vui lòng chọn ngày sinh!");
                return false;
            }
            if (ngaySinh.isAfter(LocalDate.now())) {
                statusLabel.setText("Ngày sinh không hợp lệ!");
                return false;
            }
            if (gioiTinhField.getValue() == null) {
                statusLabel.setText("Vui lòng chọn giới tính!");
                return false;
            }
            if (trangThaiTamVangField.getValue() == null) {
                statusLabel.setText("Vui lòng chọn trạng thái tạm vắng!");
                return false;
            }
            if (trangThaiXacThucField.getValue() == null) {
                statusLabel.setText("Vui lòng chọn trạng thái xác thực!");
                return false;
            }
            if (soPhongField.getValue() == null) {
                statusLabel.setText("Vui lòng chọn số phòng!");
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi kiểm tra dữ liệu: " + e.getMessage());
            return false;
        }
    }
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @FXML
    private void handleCancel() {
        if (stage != null) {
            stage.close();
        }
    }
}