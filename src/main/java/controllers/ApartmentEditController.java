package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import models.Apartment;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Text;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import javafx.scene.control.*;
import java.net.http.HttpClient;

@Controller
public class ApartmentEditController {
    private Stage stage;
    private Apartment apartment;

    @FXML
    private TextField soPhongField;
    @FXML
    private TextField floorField;
    @FXML
    private TextField onwerField;
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

    private final HttpClient hhtpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @FXML
    public void initialize() {
        // Thiết lập placeholder cho các trường nhập liệu
        soPhongField.setPromptText("Số phòng");
        floorField.setPromptText("Tầng");
        onwerField.setPromptText("Chủ sở hữu");
        dienTichField.setPromptText("Diện tích (m²)");
        soXeMayField.setPromptText("Số xe máy");
        soOToField.setPromptText("Số ô tô");
        soPhongNguField.setPromptText("Số phòng ngủ");
        soPhongTamField.setPromptText("Số phòng tắm");

        // Xóa nhãn trạng thái ban đầu
        statusLabel.setText("");

        // Gắn sự kiện cho nút Save
        SaveButton.setOnAction(event -> handleSave());

        // Gắn sự kiện cho nút Cancel
        cancelButton.setOnAction(event -> handleCancel());

        // Ràng buộc định dạng: chỉ cho phép nhập số trong các trường số
        addNumericValidation(floorField);
        addNumericValidation(dienTichField);
        addNumericValidation(soXeMayField);
        addNumericValidation(soOToField);
        addNumericValidation(soPhongNguField);
        addNumericValidation(soPhongTamField);

        // Ràng buộc định dạng: chỉ cho phép nhập chữ và số cho onwerField (có thể điều chỉnh theo nhu cầu)
        onwerField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[\\p{L}\\s]+")) {
                onwerField.setText(oldVal);
            }
        });
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
//            // Populate dữ liệu vào các trường nhập liệu
//            soPhongField.setText(apartment.getRoomNumber());
//            floorField.setText(String.valueOf(apartment.getFloor()));
//            onwerField.setText(apartment.getOwner() != null ? apartment.getOwner().getUsername() : ""); // Cần phương thức getFullName()
//            dienTichField.setText(String.valueOf(apartment.getArea()));
//            soXeMayField.setText(String.valueOf(apartment.getMotorbikeCount()));
//            soOToField.setText(String.valueOf(apartment.getCarCount()));
//            soPhongNguField.setText(String.valueOf(apartment.getBedroomCount()));
//            soPhongTamField.setText(String.valueOf(apartment.getBathroomCount()));
            populateForm(apartment);
        }

        // Nếu mode == 1, chuyển sang chế độ chỉ xem
        if (mode == 1) {
            soPhongField.setDisable(true);
            floorField.setDisable(true);
            onwerField.setDisable(true);
            dienTichField.setDisable(true);
            soXeMayField.setDisable(true);
            soOToField.setDisable(true);
            soPhongNguField.setDisable(true);
            soPhongTamField.setDisable(true);

            SaveButton.setVisible(false);
        }
    }

    public Apartment getApartment() {return apartment;}

    private void populateForm(Apartment apartment) {
        if (apartment == null) {
            System.err.println("Apartment is null!");
            return;
        }

        try {
            // Ghi log chi tiết để kiểm tra giá trị
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
            onwerField.setText(apartment.getOwner() != null ? apartment.getOwner().getUsername() : "");
            dienTichField.setText(String.valueOf(apartment.getArea()));
            soXeMayField.setText(String.valueOf(apartment.getMotorbikeCount()));
            soOToField.setText(String.valueOf(apartment.getCarCount()));
            soPhongNguField.setText(String.valueOf(apartment.getBedroomCount()));
            soPhongTamField.setText(String.valueOf(apartment.getBathroomCount()));

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
            // Kiểm tra dữ liệu nhập vào
            if (!validateApartmentInput()) {
                statusLabel.setText("Vui lòng điền đầy đủ thông tin hợp lệ.");
                return;
            }

            boolean inserted = false;

            // Nếu chưa có apartment, khởi tạo mới
            if (apartment == null) {
                apartment = new Apartment();
                inserted = true;
            }

            // Cập nhật dữ liệu từ form vào đối tượng apartment
            apartment.setRoomNumber(soPhongField.getText());
            apartment.setFloor(Integer.parseInt(floorField.getText()));
            apartment.setArea(Float.parseFloat(dienTichField.getText()));
            apartment.setMotorbikeCount(Integer.parseInt(soXeMayField.getText()));
            apartment.setCarCount(Integer.parseInt(soOToField.getText()));
            apartment.setBedroomCount(Integer.parseInt(soPhongNguField.getText()));
            apartment.setBathroomCount(Integer.parseInt(soPhongTamField.getText()));

            // Gán resident owner nếu có (tuỳ logic bạn xử lý owner)
//            if (selectedOwner != null) {
//                apartment.setOwner(selectedOwner);
//            }

            // In log để kiểm tra dữ liệu
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

            HttpResponse<String> response = hhtpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ;

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
            // Kiểm tra số phòng
            if (isNullOrEmpty(soPhongField.getText())) {
                statusLabel.setText("Số phòng không được để trống!");
                return false;
            }

            // Kiểm tra tầng
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

            // Kiểm tra diện tích
            if (isNullOrEmpty(dienTichField.getText())) {
                statusLabel.setText("Diện tích không được để trống!");
                return false;
            }
            try {
                double area = Double.parseDouble(dienTichField.getText());
                if (area <= 0) {
                    statusLabel.setText("Diện tích phải lớn hơn 0!");
                    return false;
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Diện tích phải là số!");
                return false;
            }

            // Kiểm tra số lượng xe máy
            if (!isValidIntegerField(soXeMayField.getText(), "Số xe máy")) return false;

            // Kiểm tra số lượng ô tô
            if (!isValidIntegerField(soOToField.getText(), "Số ô tô")) return false;

            // Kiểm tra số phòng ngủ
            if (!isValidIntegerField(soPhongNguField.getText(), "Số phòng ngủ")) return false;

            // Kiểm tra số phòng tắm
            if (!isValidIntegerField(soPhongTamField.getText(), "Số phòng tắm")) return false;

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
            int intValue = Integer.parseInt(value);
            if (intValue < 0) {
                statusLabel.setText(fieldName + " không được âm!");
                return false;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText(fieldName + " phải là số nguyên!");
            return false;
        }
        return true;
    }

    @FXML
    private void handleCancel() {
        if (stage != null) {
            stage.close();
        }
    }



}
