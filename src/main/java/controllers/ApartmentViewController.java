package controllers;

import app.MainApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Apartment;
import models.Resident;
import models.enums.Role;
import models.enums.SoPhong;
import models.enums.XacThuc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import services.ApartmentService;
import utils.UserUtils;
import utils.SoPhongUtil;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.fxml.*;
@Controller
public class ApartmentViewController {
    private final ApartmentService apartmentService;
    private final ApartmentEditController apartmentEditController;

    @Autowired
    public ApartmentViewController(ApartmentService apartmentService, ApartmentEditController apartmentEditController) {
        this.apartmentEditController = apartmentEditController;
        this.apartmentService = apartmentService;
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final Set<Role> adminRoles = Set.of(Role.ADMIN, Role.ADMIN_ROOT);

    @FXML
    private TableView<Apartment> apartmentTable;
    @FXML
    private TableColumn<Apartment, String> ownerUsernameColumn;
    @FXML
    private TableColumn<Apartment, Integer> floorColumn;
    @FXML
    private TableColumn<Apartment, String> roomNumberColumn;
    @FXML
    private TableColumn<Apartment, Float> areaColumn;
    @FXML
    private TableColumn<Apartment, Integer> motorbikeCountColumn;
    @FXML
    private TableColumn<Apartment, Integer> carCountColumn;
    @FXML
    private TableColumn<Apartment, Integer> bedroomCountColumn;
    @FXML
    private TableColumn<Apartment, Integer> bathroomCountColumn;
    @FXML
    private TableColumn<Apartment, Void> actionColumn;
    @FXML
    private ComboBox<String> floorComboBox;
    @FXML
    private ComboBox<String> roomComboBox; //chưa chắc đã cần tìm theo phòng
    @FXML
    private ComboBox<String> ownerComboBox; // chưa chắc đã dùng tới
    @FXML
    private Label thongBaoLabel;
    @FXML
    private Button resetButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button seeAll;

    private ObservableList<Apartment> apartmentList = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        Role currentRole = UserUtils.getCurrentUser().getRole();
        System.out.println("Vai trò người dùng hiện tại: " + currentRole);

        if (currentRole == Role.USER) {
            // Nếu là USER thì chỉ được xem thông tin căn hộ của mình
            List<Apartment> userApartments = apartmentService.getAllApartmentsByOwner(UserUtils.getCurrentUser());

            if (!userApartments.isEmpty()) {
                apartmentTable.setItems(FXCollections.observableArrayList(userApartments));
                apartmentTable.setVisible(true);
                thongBaoLabel.setVisible(false);
            } else {
                thongBaoLabel.setText("Không tìm thấy căn hộ của bạn.");
                thongBaoLabel.setVisible(true);
                apartmentTable.setVisible(false);
            }
            return; // Không khởi tạo các ComboBox hay bảng đầy đủ nếu là user thường
        }

        // Nếu là ADMIN hoặc ADMIN_ROOT thì load toàn bộ danh sách căn hộ
        List<Apartment> allApartments = apartmentService.getAllApartments();
        apartmentTable.setItems(FXCollections.observableArrayList(allApartments));
        apartmentTable.setVisible(true);
        thongBaoLabel.setVisible(false);

        // Khởi tạo ComboBox tầng (Floor)
        ObservableList<String> floorOptions = FXCollections.observableArrayList();
        for (int i = 5; i <= 28; i++) { // Tầng 2 đến 29 là tầng căn hộ
            floorOptions.add("Tầng " + i);
        }
        floorComboBox.setItems(floorOptions);
        floorComboBox.setValue(null);

        // Khởi tạo ComboBox số phòng (Room Number)
        roomComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(SoPhong.values())
                        .map(Enum::name)
                        .collect(Collectors.toList())
        ));
        roomComboBox.setValue(null);

        // Sự kiện chọn tầng hoặc phòng
        floorComboBox.setOnAction(event -> handleSearch());
        roomComboBox.setOnAction(event -> handleSearch());

        // Cài đặt cột bảng
        ownerUsernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getOwner() != null ? cellData.getValue().getOwner().getUsername() : ""
        ));
        floorColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFloor()));
        roomNumberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomNumber()));
        areaColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getArea()));
        motorbikeCountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getMotorbikeCount()));
        carCountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCarCount()));
        bedroomCountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBedroomCount()));
        bathroomCountColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBathroomCount()));

        apartmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Cột hành động
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Xem");
            private final Button editButton = new Button("Sửa");
            private final HBox pane = new HBox(viewButton, editButton);

            {
                viewButton.setOnAction(event -> {
                    Apartment apartment = getTableView().getItems().get(getIndex());
                    handleViewDetails(apartment);
                });

                editButton.setOnAction(event -> {
                    Apartment apartment = getTableView().getItems().get(getIndex());
                    handleEdit(apartment);
                });

                pane.setSpacing(5);
                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }


    private void loadApartments() {
        try {
            System.out.println("Đang gửi yêu cầu HTTP đến API...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/apartments"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // In ra trạng thái của request
            System.out.println("Trạng thái của request: " + response.statusCode());
            System.out.println("Nội dung phản hồi: " + response.body());

            if (response.statusCode() == 200) {
                Apartment[] apartments = objectMapper.readValue(response.body(), Apartment[].class);

                // Xóa danh sách căn hộ hiện tại để cập nhật lại
                apartmentList.clear();

                for (Apartment apartment : apartments) {
                    System.out.println("Đang xử lý căn hộ: " + apartment.getRoomNumber() + " (ID: " + apartment.getId() + ")");
                    System.out.println("Số phòng ngủ: " + apartment.getBedroomCount());
                    System.out.println("Số phòng tắm: " + apartment.getBathroomCount());

                    // Lấy căn hộ từ cơ sở dữ liệu
                    Apartment existingApartment = apartmentService.getApartmentById(apartment.getId());
                    if (existingApartment != null) {
                        System.out.println("Căn hộ trong DB trước khi cập nhật: " + existingApartment);

                        // Cập nhật căn hộ nếu có sự thay đổi
                        existingApartment.setRoomNumber(apartment.getRoomNumber());
                        existingApartment.setFloor(apartment.getFloor());
                        existingApartment.setOwner(apartment.getOwner());
                        existingApartment.setArea(apartment.getArea());
                        existingApartment.setMotorbikeCount(apartment.getMotorbikeCount());
                        existingApartment.setCarCount(apartment.getCarCount());
                        existingApartment.setBathroomCount(apartment.getBathroomCount());
                        existingApartment.setBedroomCount(apartment.getBedroomCount());

                        // Cập nhật căn hộ nếu có sự thay đổi
                        apartmentService.updateApartment(existingApartment);

                        System.out.println("Căn hộ trong DB sau khi cập nhật: " + existingApartment);
                    } else {
                        System.out.println("Căn hộ không tìm thấy trong DB, cần kiểm tra.");
                    }

                    // Thêm căn hộ vào danh sách để hiển thị
                    apartmentList.add(existingApartment);
                }

                // In ra các thông tin trong apartmentList sau khi đã thêm
                for (Apartment apartment : apartmentList) {
                    System.out.println("Căn hộ trong danh sách: " + apartment.getRoomNumber() +
                            ", Số phòng ngủ: " + apartment.getBedroomCount() +
                            ", Số phòng tắm: " + apartment.getBathroomCount());
                }

                apartmentTable.setItems(apartmentList);
                thongBaoLabel.setText("Tải lên dữ liệu căn hộ thành công.");
            } else {
                thongBaoLabel.setText("Tải lên dữ liệu căn hộ thất bại.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi chi tiết
            thongBaoLabel.setText("Lỗi tải lên dữ liệu căn hộ!");
        }
    }


    private void openApartmentStage(Apartment apartment, int mode) {
        try {
            System.out.println("Bắt đầu mở cửa sổ căn hộ. Mode = " + mode);

            // Tạo FXMLLoader và nạp fxml view
            System.out.println("Đang tải file FXML: /view/apartment/apartment_edit.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/apartment/apartment_edit.fxml"));

            // Thiết lập controller factory sử dụng Spring context để quản lý bean
            System.out.println("Thiết lập controller factory từ Spring context");
            fxmlLoader.setControllerFactory(MainApplication.springContext::getBean);

            // Tạo Scene từ fxml đã load, kích thước của cửa sổ
            System.out.println("Đang load FXML và tạo Scene");
            Scene apartmentScene = new Scene(fxmlLoader.load(), 875, 415);

            // Tạo một cửa sổ mới cho chế độ chỉnh sửa căn hộ
            Stage apartmentEditStage = new Stage();
            apartmentEditStage.initModality(Modality.APPLICATION_MODAL);

            // Thiết lập tiêu đề cửa sổ theo chế độ
            switch (mode) {
                case 1:
                    apartmentEditStage.setTitle("Chi tiết căn hộ");
                    break;
                case 2:
                    apartmentEditStage.setTitle("Thêm mới căn hộ");
                    break;
                case 3:
                    apartmentEditStage.setTitle("Cập nhật căn hộ");
                    break;
                default:
                    apartmentEditStage.setTitle("Quản lý căn hộ");
                    break;
            }
            System.out.println("Đã thiết lập tiêu đề cửa sổ: " + apartmentEditStage.getTitle());

            // Gắn Scene vào Stage
            apartmentEditStage.setScene(apartmentScene);
            System.out.println("Đã gắn Scene vào Stage");

            // Lấy controller của fxml đã load và thiết lập các giá trị cần thiết
            ApartmentEditController apartmentEditController = fxmlLoader.getController();
            System.out.println("Đã lấy được controller: " + apartmentEditController.getClass().getName());

            apartmentEditController.setStage(apartmentEditStage);
            System.out.println("Đã thiết lập Stage cho controller");

            // Thiết lập apartment và mode cho controller
            apartmentEditController.setApartment(apartment, mode);
            System.out.println("Đã thiết lập Apartment và mode cho controller");

            // Gọi populateOwnerComboBox() sau khi apartment đã được thiết lập
            apartmentEditController.populateOwnerComboBox();
            System.out.println("Đã gọi populateOwnerComboBox()");

            // Hiển thị cửa sổ và chờ người dùng tương tác (modal)
            apartmentEditStage.showAndWait();
            System.out.println("Cửa sổ căn hộ đã hiển thị xong");
        } catch (Exception e) {
            System.out.println("Đã xảy ra lỗi khi mở cửa sổ căn hộ: " + e.getMessage());
            e.printStackTrace();
            Logger.getLogger(ApartmentController.class.getName()).log(Level.SEVERE, "Lỗi khi mở cửa sổ căn hộ", e);
        }
    }


    @FXML
    private void handleEdit(Apartment selectedApartment) {
        if (selectedApartment == null) {
            thongBaoLabel.setText("Chọn căn hộ để cập nhật!");
            return;
        }

        openApartmentStage(selectedApartment, 3); // Mode 3: cập nhật

        Apartment updatedApartment = apartmentEditController.getApartment();
        if (updatedApartment != null && !updatedApartment.equals(selectedApartment)) {
            for (int i = 0; i < apartmentList.size(); i++) {
                if (apartmentList.get(i).getId().equals(updatedApartment.getId())) {
                    apartmentList.set(i, updatedApartment);
                    break;
                }
            }
            apartmentTable.refresh();
            thongBaoLabel.setText("Cập nhật căn hộ thành công!");
        }
    }

    @FXML
    private void handleViewDetails(Apartment apartment) {
        openApartmentStage(apartment, 1);
    }


    @FXML
    private void handleSearch() {
        // Lấy giá trị từ ComboBox
        String filterRoom = roomComboBox.getValue();
        String filterFloor = floorComboBox.getValue();

        System.out.println("== ComboBox Values ==");
        System.out.println("Room: " + filterRoom);
        System.out.println("Floor: " + filterFloor);

        // Tạo URL cơ bản
        String baseUrl = "http://localhost:8080/api/apartments";
        List<String> queryParams = new ArrayList<>();

        // Xử lý số phòng (roomNumber)
        if (filterRoom != null && !filterRoom.isEmpty()) {
            String roomNumber = filterRoom.replace("PHONG_", "").replace("_", "");
            queryParams.add("roomNumber=" + URLEncoder.encode(roomNumber, StandardCharsets.UTF_8));
            System.out.println("Added roomNumber filter: " + roomNumber);  // Debug line
        }

        // Xử lý tầng (floor)
        if (filterFloor != null && !filterFloor.isEmpty()) {
            String floor = filterFloor.replace("Tầng ", "");
            queryParams.add("floor=" + URLEncoder.encode(floor, StandardCharsets.UTF_8));
            System.out.println("Added floor filter: " + floor);  // Debug line
        }

        // Nếu có bất kỳ tham số nào, tạo query string
        String url = baseUrl;
        if (!queryParams.isEmpty()) {
            url += "?" + String.join("&", queryParams);
        }

        System.out.println("== Final Request URL ==");
        System.out.println(url);  // Debug line

        // Thực hiện yêu cầu HTTP
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("== Response Status ==");
            System.out.println(response.statusCode());  // Debug line

            if (response.statusCode() == 200) {
                System.out.println("== Raw JSON Response ==");
                System.out.println(response.body());  // Debug line

                Apartment[] filteredApartments = objectMapper.readValue(response.body(), Apartment[].class);

                System.out.println("== Parsed Apartment Count ==");
                System.out.println(filteredApartments.length);  // Debug line

                apartmentList.setAll(filteredApartments);
                apartmentTable.setItems(apartmentList);
            } else {
                System.out.println("Error: Received HTTP status " + response.statusCode());
            }

        } catch (Exception e) {
            System.out.println("== Exception Occurred ==");
            e.printStackTrace();  // Debug line
        }
    }




    @FXML
    private void handleResetSearch() {
        roomComboBox.setValue(null);
        floorComboBox.setValue(null);
        loadApartments();
    }

}
