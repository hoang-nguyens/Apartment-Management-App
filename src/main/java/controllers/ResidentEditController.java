package controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import javafx.stage.Stage;
import models.Resident;
import models.User;
import models.enums.Gender;
import models.enums.SoPhong;
import models.enums.TamVangStatus;
import models.enums.XacThuc;
import org.springframework.stereotype.Controller;

import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ResidentEditController{
    private Stage stage;
    private Resident resident;

    public ResidentEditController() {}
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
        // ComboBox thiết lập với các enum
        gioiTinhField.getItems().addAll(Gender.values()); // Thêm các giá trị enum của Gender
        gioiTinhField.setPromptText(""); // Thiết lập thông báo cho ComboBox

        trangThaiTamVangField.getItems().addAll(TamVangStatus.values()); // Thêm các giá trị enum của TamVangStatus
        trangThaiTamVangField.setPromptText(""); // Thiết lập thông báo cho ComboBox

        soPhongField.getItems().addAll(SoPhong.values()); // Thêm các giá trị enum của SoPhong
        soPhongField.setPromptText(""); // Thiết lập thông báo cho ComboBox

        trangThaiXacThucField.getItems().addAll(XacThuc.values());
        trangThaiXacThucField.setPromptText("");

        // Prompt text cho các trường nhập liệu
        nameField.setPromptText("Nhập họ tên");
        usenameField.setPromptText("Tên đăng nhập");
        cccdField.setPromptText("Mã số CCCD");
        stdField.setPromptText("Số điện thoại");
        ngaysinhField.setPromptText("mm/dd/yy");

        // Giới hạn CCCD (chỉ cho phép nhập số)
        cccdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                cccdField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (cccdField.getText().length() > 12) {
                cccdField.setText(cccdField.getText(0, 12));
            }
        });

// Giới hạn số điện thoại (chỉ cho phép nhập số)
        stdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                stdField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (stdField.getText().length() > 11) {
                stdField.setText(stdField.getText(0, 11));
            }
        });


        // Trạng thái ban đầu
        statusLabel.setText(""); // Xóa thông báo trạng thái
    }



    public void setStage(Stage stage) {
        this.stage = stage;

    }


    public void setResident(Resident resident, int mode) {
        // Gán đối tượng Resident cho biến trong Controller
        this.resident = resident;

        // Nếu đối tượng resident không null, populate dữ liệu vào form
        if (resident != null) {
            // Populate form với dữ liệu từ resident
            populateForm(resident);

            // Cập nhật các ComboBox với dữ liệu từ resident
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
        usenameField.setDisable(true);
        // Nếu mode == 1, chuyển form sang chế độ chỉ xem (read-only)
        if (mode == 1) {
            // Vô hiệu hóa các trường input
            nameField.setDisable(true);
            usenameField.setDisable(true);
            cccdField.setDisable(true);
            stdField.setDisable(true);
            ngaysinhField.setDisable(true);
            gioiTinhField.setDisable(true);
            trangThaiTamVangField.setDisable(true);
            trangThaiXacThucField.setDisable(true);
            soPhongField.setDisable(true);

            // Ẩn nút Save
            saveButton.setVisible(false);
        }
    }




    public Resident getResident() {return resident;}



    private void populateForm(Resident resident) {
        if (resident == null) {
            System.err.println("Resident is null!");
            return;
        }

        try {
            // Ghi log chi tiết để kiểm tra giá trị
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

            // Điền thông tin cơ bản
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
            // Kiểm tra tính hợp lệ của dữ liệu đầu vào
            if (!validateResidentInput()) {
                statusLabel.setText("Vui lòng điền đầy đủ thông tin hợp lệ.");
                return;
            }

            boolean inserted = false;

            // Nếu resident là null, tạo mới đối tượng resident
            if (resident == null) {
                resident = new Resident();
                resident.setUser(new User()); // Sửa lỗi NullPointerException
                inserted = true;
            }

            // Cập nhật dữ liệu từ form
            resident.setHoTen(nameField.getText());
            resident.getUser().setUsername(usenameField.getText());
            resident.setCccd(cccdField.getText());
            resident.setSdt(stdField.getText());
            resident.setNgaySinh(ngaysinhField.getValue());
            resident.setGioiTinh(gioiTinhField.getValue());
            resident.setTrangThaiTamVang(trangThaiTamVangField.getValue());
            resident.setTrangThaiXacThuc(trangThaiXacThucField.getValue());
            resident.setSoPhong(soPhongField.getValue());

            // Kiểm tra lại dữ liệu của resident trước khi chuyển thành JSON
            if (resident.getHoTen() == null || resident.getHoTen().isEmpty() ||
                    resident.getUser().getUsername() == null || resident.getUser().getUsername().isEmpty() ||
                    resident.getCccd() == null || resident.getCccd().isEmpty()) {
                statusLabel.setText("Thông tin không hợp lệ! Vui lòng kiểm tra lại.");
                return;
            }

            // In ra dữ liệu resident để kiểm tra
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

            // Chuyển đối tượng resident thành JSON
            String requestBody = objectMapper.writeValueAsString(resident);
            System.out.println("Request Body (JSON): " + requestBody); // In ra requestBody để kiểm tra

            // Tạo HTTP request
            HttpRequest request;
            if (inserted) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/residents"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
            } else {
                // Kiểm tra nếu id của resident không hợp lệ, không thực hiện PUT
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

            // Gửi request
            HttpResponse<String> response = hhtpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // In ra thông tin phản hồi từ server
            System.out.println("Response status code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            // Xử lý phản hồi
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
            // Kiểm tra họ tên
            if (isNullOrEmpty(nameField.getText())) {
                statusLabel.setText("Họ tên không được để trống!");
                return false;
            }

            // Kiểm tra username
            if (isNullOrEmpty(usenameField.getText())) {
                statusLabel.setText("Username không được để trống!");
                return false;
            }

            // Kiểm tra CCCD: không được để trống và phải đủ 12 chữ số
            String cccd = cccdField.getText();
            if (isNullOrEmpty(cccd)) {
                statusLabel.setText("CCCD không được để trống!");
                return false;
            }
            if (!cccd.matches("\\d{12}")) {
                statusLabel.setText("CCCD phải bao gồm đúng 12 chữ số!");
                return false;
            }

            // Kiểm tra số điện thoại: không được để trống và từ 10–11 chữ số
            String sdt = stdField.getText();
            if (isNullOrEmpty(sdt)) {
                statusLabel.setText("Số điện thoại không được để trống!");
                return false;
            }
            if (!sdt.matches("\\d{10,11}")) {
                statusLabel.setText("Số điện thoại phải bao gồm 10–11 chữ số!");
                return false;
            }

            // Kiểm tra ngày sinh: phải được chọn và không được lớn hơn ngày hiện tại
            LocalDate ngaySinh = ngaysinhField.getValue();
            if (ngaySinh == null) {
                statusLabel.setText("Vui lòng chọn ngày sinh!");
                return false;
            }
            if (ngaySinh.isAfter(LocalDate.now())) {
                statusLabel.setText("Ngày sinh không hợp lệ!");
                return false;
            }

            // Kiểm tra giới tính
            if (gioiTinhField.getValue() == null) {
                statusLabel.setText("Vui lòng chọn giới tính!");
                return false;
            }

            // Kiểm tra trạng thái tạm vắng
            if (trangThaiTamVangField.getValue() == null) {
                statusLabel.setText("Vui lòng chọn trạng thái tạm vắng!");
                return false;
            }

            // Kiểm tra trạng thái xác thực
            if (trangThaiXacThucField.getValue() == null) {
                statusLabel.setText("Vui lòng chọn trạng thái xác thực!");
                return false;
            }

            // Kiểm tra số phòng
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

    // Hàm phụ trợ kiểm tra chuỗi null hoặc rỗng
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