package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.Apartment;
import models.enums.SoPhong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Text;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Optional;

import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import javafx.scene.control.*;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import repositories.*;
import models.*;
@Controller
public class ApartmentEditController {
    private Stage stage;
    private Apartment apartment;

    @FXML
    private TextField soPhongField;
    @FXML
    private TextField floorField;
    @FXML
    private ComboBox<User> ownerComboBox;
    @FXML
    private TextField dienTichField;
    @FXML
    private TextField soXeMayField;
    @FXML
    private TextField soOToField;
    @FXML
    private TextField soPhongNguField;
    @FXML
    private TextField soPhongTamField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button SaveButton;
    @FXML
    private Button cancelButton;


    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private ResidentRepository residentRepository;
    @Autowired
    private UserRepository userRepository;




    @FXML
    public void initialize() {
        // Thiết lập placeholder cho các trường nhập liệu
        soPhongField.setPromptText("Số phòng");
        floorField.setPromptText("Tầng");
        ownerComboBox.setPromptText("Chủ sở hữu");
        dienTichField.setPromptText("Diện tích (m²)");
        soXeMayField.setPromptText("Số xe máy");
        soOToField.setPromptText("Số ô tô");
        soPhongNguField.setPromptText("Số phòng ngủ");
        soPhongTamField.setPromptText("Số phòng tắm");

        // Xóa nhãn trạng thái ban đầu
        statusLabel.setText("");

        // Gắn sự kiện cho các nút
        SaveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());

        // Ràng buộc định dạng số
        addNumericValidation(floorField);
        addNumericValidation(soXeMayField);
        addNumericValidation(soOToField);
        addNumericValidation(soPhongNguField);
        addNumericValidation(soPhongTamField);

        // Gắn sự kiện chọn User trong ComboBox
        ownerComboBox.setOnAction(event -> {
            User selectedUser = ownerComboBox.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                apartment.setOwner(selectedUser);  // Chỉ thay đổi chủ sở hữu
            }
        });

        // Kiểm tra xem apartment có được khởi tạo chưa
        if (apartment != null) {
            // Hiển thị thông tin số phòng
            soPhongField.setText(String.valueOf(apartment.getRoomNumber()));  // Chỉ hiển thị, không thay đổi
            // Nạp dữ liệu vào ComboBox chủ hộ
            populateOwnerComboBox();
        }
    }



    // Danh sách resident được lưu tạm để tìm sau khi chọn
    private List<Resident> residentsInApartment = new ArrayList<>();

    private void populateOwnerComboBox() {
        // Kiểm tra apartment đã được khởi tạo chưa
        if (apartment == null) {
            System.out.println("Apartment là null!");
            return;
        }

        // Convert "0501" => PHONG_0501
        String phongEnumName = "PHONG_" + apartment.getRoomNumber();
        System.out.println("Tên enum phòng: " + phongEnumName);

        SoPhong soPhongEnum = SoPhong.valueOf(phongEnumName);

        // Kiểm tra cư dân trong phòng
        residentsInApartment = residentRepository.findBySoPhong(soPhongEnum);
        if (residentsInApartment == null || residentsInApartment.isEmpty()) {
            System.out.println("Không có cư dân trong phòng: " + phongEnumName);
        } else {
            System.out.println("Số cư dân tìm thấy: " + residentsInApartment.size());
            for (Resident resident : residentsInApartment) {
                System.out.println("Cư dân: " + resident.getUsername());
            }
        }

        // Lấy danh sách User từ Resident (qua username)
        List<User> usersInRoom = residentsInApartment.stream()
                .map(resident -> userRepository.findByUsername(resident.getUsername()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // Kiểm tra danh sách usersInRoom
        System.out.println("Danh sách Users tương ứng với cư dân trong phòng:");
        for (User user : usersInRoom) {
            System.out.println("User: " + user.getUsername() + " - " + user.getUsername());
        }

        // Thiết lập StringConverter để hiển thị họ tên cư dân
        ownerComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                if (user == null) return "";
                // Tìm Resident tương ứng để lấy họ tên
                return residentsInApartment.stream()
                        .filter(resident -> resident.getUsername().equals(user.getUsername()))
                        .map(Resident::getHoTen)
                        .findFirst()
                        .orElse("(Không rõ)");
            }

            @Override
            public User fromString(String string) {
                // Không cần dùng trong trường hợp này
                return null;
            }
        });

        // Nạp dữ liệu vào ComboBox
        ownerComboBox.getItems().clear();
        ownerComboBox.getItems().addAll(usersInRoom);
        System.out.println("Dữ liệu đã nạp vào ComboBox: ");
        for (User user : ownerComboBox.getItems()) {
            System.out.println("User trong ComboBox: " + user.getUsername() + " - " + user.getUsername());
        }

        // Nếu apartment đã có owner → gán vào ComboBox
        if (apartment.getOwner() != null) {
            System.out.println("Chủ sở hữu hiện tại: " + apartment.getOwner().getUsername());
            ownerComboBox.setValue(apartment.getOwner());
        } else {
            System.out.println("Căn hộ chưa có chủ sở hữu.");
        }
    }



    // Hàm ràng buộc chỉ nhập số
    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                field.setText(oldValue);
            }
        });
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setApartment(Apartment apartment, int mode) {
        this.apartment = apartment;

        if (apartment != null) {
            populateForm(apartment);
        }

        // Nếu mode == 1, chuyển sang chế độ chỉ xem (disable tất cả các trường và ẩn nút Save)
        if (mode == 1) {
            soPhongField.setDisable(true);
            floorField.setDisable(true);
            ownerComboBox.setDisable(true);
            dienTichField.setDisable(true);
            soXeMayField.setDisable(true);
            soOToField.setDisable(true);
            soPhongNguField.setDisable(true);
            soPhongTamField.setDisable(true);
            SaveButton.setVisible(false);
        }

        // mode = 3 cho phép chỉnh sửa thông tin
        else if (mode == 3) {
            soPhongField.setDisable(true);
            floorField.setDisable(true);
            ownerComboBox.setDisable(false);
            dienTichField.setDisable(true);
            soXeMayField.setDisable(false);
            soOToField.setDisable(false);
            soPhongNguField.setDisable(true);
            soPhongTamField.setDisable(true);
            SaveButton.setVisible(true);
        }
    }

    public Apartment getApartment() {return apartment;}

    private void populateForm(Apartment apartment) {
        if (apartment == null) {
            System.err.println("Apartment is null!");
            return;
        }

        try {
            System.out.println(">>> Bắt đầu populateForm (Apartment)");
            System.out.println("ID: " + apartment.getId());
            System.out.println("Số phòng: " + apartment.getRoomNumber());
            System.out.println("Tầng: " + apartment.getFloor());
            System.out.println("Chủ sở hữu: " + (apartment.getOwner() != null ? apartment.getOwner().getUsername() : "null"));
            System.out.println("Diện tích: " + apartment.getArea());
            System.out.println("Xe máy: " + apartment.getMotorbikeCount());
            System.out.println("Ô tô: " + apartment.getCarCount());
            System.out.println("Phòng ngủ: " + apartment.getBedroomCount());
            System.out.println("Phòng tắm: " + apartment.getBathroomCount());

            // Điền thông tin vào form
            soPhongField.setText(safeString(apartment.getRoomNumber()));
            floorField.setText(String.valueOf(apartment.getFloor()));
            dienTichField.setText(apartment.getArea() != null ? apartment.getArea() + " m²" : "");

            soXeMayField.setText(String.valueOf(apartment.getMotorbikeCount()));
            soOToField.setText(String.valueOf(apartment.getCarCount()));
            soPhongNguField.setText(String.valueOf(apartment.getBedroomCount()));
            soPhongTamField.setText(String.valueOf(apartment.getBathroomCount()));

            // Thiết lập StringConverter cho ComboBox<User>
            ownerComboBox.setConverter(new StringConverter<User>() {
                @Override
                public String toString(User user) {
                    return (user != null) ? user.getUsername() : "";
                }

                @Override
                public User fromString(String string) {
                    if (string != null && !string.isEmpty()) {
                        return userRepository.findByUsername(string).orElse(null);
                    }
                    return null;
                }
            });

            // Gán owner (kiểu User) cho ComboBox
            if (apartment.getOwner() != null) {
                ownerComboBox.setValue(apartment.getOwner());
            } else {
                ownerComboBox.setValue(null);
            }

            // Ghi log xác nhận
            System.out.println("soPhongField: " + soPhongField.getText());
            System.out.println("floorField: " + floorField.getText());
            System.out.println("ownerComboBox: " + ownerComboBox.getValue());
            System.out.println("dienTichField: " + dienTichField.getText());
            System.out.println("soXeMayField: " + soXeMayField.getText());
            System.out.println("soOToField: " + soOToField.getText());
            System.out.println("soPhongNguField: " + soPhongNguField.getText());
            System.out.println("soPhongTamField: " + soPhongTamField.getText());

            System.out.println(">>> Kết thúc populateForm (Apartment)");

        } catch (Exception e) {
            System.err.println("Lỗi khi populateForm Apartment: " + e.getMessage());
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
            if (!validateApartmentInput()) {
                statusLabel.setText("Vui lòng điền đầy đủ thông tin hợp lệ.");
                return;
            }

            boolean inserted = false;

            if (apartment == null) {
                apartment = new Apartment();
                inserted = true;
            }

            apartment.setRoomNumber(soPhongField.getText());
            apartment.setFloor(Integer.parseInt(floorField.getText()));

            String areaText = dienTichField.getText().replace("m²", "").trim();
            apartment.setArea(Float.parseFloat(areaText));

            apartment.setMotorbikeCount(Integer.parseInt(soXeMayField.getText()));
            apartment.setCarCount(Integer.parseInt(soOToField.getText()));
            apartment.setBedroomCount(Integer.parseInt(soPhongNguField.getText()));
            apartment.setBathroomCount(Integer.parseInt(soPhongTamField.getText()));

            // Set chủ sở hữu
            User selectedOwner = ownerComboBox.getValue();
            if (selectedOwner != null) {
                apartment.setOwner(selectedOwner);
            } else {
                statusLabel.setText("Vui lòng chọn chủ hộ.");
                return;
            }

            System.out.println("Apartment before request:");
            System.out.println("Room: " + apartment.getRoomNumber());
            System.out.println("Floor: " + apartment.getFloor());
            System.out.println("Area: " + apartment.getArea());
            System.out.println("Owner: " + (apartment.getOwner() != null ? apartment.getOwner().getUsername() : "null"));

            String requestBody = objectMapper.writeValueAsString(apartment);
            System.out.println("Request body: " + requestBody);

            HttpRequest request;
            if (inserted) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/apartments"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
            } else {
                if (apartment.getId() == null || apartment.getId() <= 0) {
                    statusLabel.setText("ID không hợp lệ.");
                    return;
                }

                request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/apartments/" + apartment.getId()))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
            }

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response status code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            if (response.statusCode() == 200) {
                apartment = objectMapper.readValue(response.body(), Apartment.class);
                statusLabel.setText("Lưu thành công!");
                stage.close();
            } else {
                statusLabel.setText("Lỗi khi lưu: " + response.body());
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("Sai định dạng số: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Lỗi: " + e.getMessage());
        }
    }

    private boolean validateApartmentInput() {
        try {
            if (isNullOrEmpty(soPhongField.getText())) {
                statusLabel.setText("Số phòng không được để trống!");
                return false;
            }

            if (isNullOrEmpty(floorField.getText())) {
                statusLabel.setText("Tầng không được để trống!");
                return false;
            }
            try {
                int floor = Integer.parseInt(floorField.getText());
                if (floor <= 0) {
                    statusLabel.setText("Tầng phải lớn hơn 0!");
                    return false;
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Tầng phải là số nguyên!");
                return false;
            }

            if (isNullOrEmpty(dienTichField.getText())) {
                statusLabel.setText("Diện tích không được để trống!");
                return false;
            }
            try {
                String areaText = dienTichField.getText().replace("m²", "").trim();
                double area = Double.parseDouble(areaText);
                if (area <= 0) {
                    statusLabel.setText("Diện tích phải lớn hơn 0!");
                    return false;
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Diện tích phải là số!");
                return false;
            }

            if (!isValidIntegerField(soXeMayField.getText(), "Số xe máy")) return false;
            if (!isValidIntegerField(soOToField.getText(), "Số ô tô")) return false;
            if (!isValidIntegerField(soPhongNguField.getText(), "Số phòng ngủ")) return false;
            if (!isValidIntegerField(soPhongTamField.getText(), "Số phòng tắm")) return false;

            // Kiểm tra đã chọn chủ hộ chưa
            if (ownerComboBox.getValue() == null) {
                statusLabel.setText("Vui lòng chọn chủ hộ!");
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

    private boolean isValidIntegerField(String value, String fieldName) {
        if (isNullOrEmpty(value)) {
            statusLabel.setText(fieldName + " không được để trống!");
            return false;
        }
        try {
            int intValue = Integer.parseInt(value.trim());
            if (intValue < 0) {
                statusLabel.setText(fieldName + " không được âm!");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            statusLabel.setText(fieldName + " phải là số nguyên hợp lệ!");
            return false;
        }
    }


    @FXML
    private void handleCancel() {
        if (stage != null) {
            stage.close();
        }
    }



}
