package controllers;

import app.MainApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.Table;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Fee;
import models.Resident;
import models.User;
import models.enums.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import services.ResidentService;
import utils.UserUtils;

import javafx.scene.control.Button;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class ResidentViewController {
    private final ResidentService residentService;
    private final ResidentEditController residentEditController;

    @Autowired
    public ResidentViewController(ResidentService residentService, ResidentEditController residentEditController) {
        this.residentService = residentService;
        this.residentEditController = residentEditController;
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final Set<Role> adminRoles = Set.of(Role.ADMIN, Role.ADMIN_ROOT);

    @FXML
    private TableView<Resident> residentTable;
    @FXML
    private TableColumn<Resident, String> usernameColumn;
    @FXML
    private TableColumn<Resident, String> hotenColumn;
    @FXML
    private TableColumn<Resident, String> cccdColumn;
    @FXML
    private TableColumn<Resident, String> sdtColumn;
    @FXML
    private TableColumn<Resident, Gender> genderColumn;
    @FXML
    private TableColumn<Resident, SoPhong> apartmentNumberColumn;
    @FXML
    private TableColumn<Resident, LocalDate> dobColumn;
    @FXML
    private TableColumn<Resident, TamVangStatus> tamVangStatusColumn;
    @FXML
    private TableColumn<Resident, XacThuc> xacThucStatusColumn;
    @FXML
    private TableColumn<Resident, Void> actionColumn;
    @FXML
    private ComboBox<SoPhong> soPhongComboBox;
    @FXML
    private ComboBox<TamVangStatus> tamVangStatusComboBox;
    @FXML
    private ComboBox<XacThuc> xacThucStatusComboBox;
    @FXML
    private ComboBox<String> roomComboBox;
    @FXML
    private ComboBox<String> xacThucStatusSearchComboBox;
    @FXML
    private ComboBox<String> tamVangStatusSearchComboBox;
    @FXML
    private Label thongBaoLabel;
    @FXML
    private Button addButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button resetButton;

    private ObservableList<Resident> residentList = FXCollections.observableArrayList();
    private ObservableList<String> categoryList = FXCollections.observableArrayList();
    private ObservableList<String> subCategoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Role currentRole = UserUtils.getCurrentUser().getRole();
        System.out.println("Vai trò người dùng hiện tại: " + currentRole);

        if (currentRole == Role.USER) {
            // Nếu là USER thì chỉ được xem thông tin cá nhân của mình
            String username = UserUtils.getCurrentUser().getUsername();
            Resident resident = residentService.findResidentByUsername(username);

            if (resident != null) {
                openResidentStage(resident, 1); // Chế độ chỉ xem
            } else {
                thongBaoLabel.setText("Không tìm thấy thông tin cư dân.");
            }

            // Ẩn bảng danh sách nếu là user thường
            residentTable.setVisible(false);
            thongBaoLabel.setVisible(false); // Hoặc có thể hiển thị lời chào khác
            return;
        }

        // Nếu là ADMIN hoặc ADMIN_ROOT thì load toàn bộ danh sách cư dân
        loadResidents();
        System.out.println("ResidentViewController initialized");

// Khởi tạo ComboBox cho số phòng (RoomNumber)
        roomComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(SoPhong.values())  // Lấy các giá trị từ enum RoomNumber
                        .map(Enum::name)  // Lấy tên enum, ví dụ: "ROOM_101"
//                        .map(this::formatEnumToDisplayName)  // Chuyển thành chuỗi dễ đọc, ví dụ: "Room 101"
                        .collect(Collectors.toList())
        ));

        // Khởi tạo ComboBox cho trạng thái xác thực (AuthStatus)
        xacThucStatusSearchComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(XacThuc.values())  // Lấy các giá trị từ enum AuthStatus
                        .map(Enum::name)  // Lấy tên enum, ví dụ: "VERIFIED"
//                        .map(this::formatEnumToDisplayName)  // Chuyển thành chuỗi dễ đọc, ví dụ: "Verified"
                        .collect(Collectors.toList())
        ));

        // Khởi tạo ComboBox cho trạng thái tạm vắng (TamVangStatus)
        tamVangStatusSearchComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(TamVangStatus.values())  // Lấy các giá trị từ enum TamVangStatus
                        .map(Enum::name)  // Lấy tên enum, ví dụ: "ACTIVE"
//                        .map(this::formatEnumToDisplayName)  // Chuyển thành chuỗi dễ đọc, ví dụ: "Active"
                        .collect(Collectors.toList())
        ));

        // Thêm các sự kiện lựa chọn nếu cần (ví dụ: xử lý khi người dùng chọn)
        roomComboBox.setOnAction(event -> handleSearch());
        xacThucStatusSearchComboBox.setOnAction(event -> handleSearch());
        tamVangStatusSearchComboBox.setOnAction(event -> handleSearch());
        // Setup các cột trong bảng
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUser().getUsername()));
        hotenColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoTen()));
        cccdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCccd()));
        sdtColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSdt()));
        genderColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getGioiTinh()));
        apartmentNumberColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSoPhong()));
        dobColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getNgaySinh()));
        tamVangStatusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTrangThaiTamVang()));
        xacThucStatusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTrangThaiXacThuc()));

        // Cột Hành động
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Xem");
            private final Button editButton = new Button("Sửa");
            private final Button deleteButton = new Button("Xóa");
            private final HBox pane = new HBox(viewButton, editButton, deleteButton);

            {
                viewButton.setOnAction(event -> {
                    Resident resident = getTableView().getItems().get(getIndex());
                    handleViewDetails(resident);
                });

                editButton.setOnAction(event -> {
                    Resident resident = getTableView().getItems().get(getIndex());
                    handleEdit(resident);
                });

                deleteButton.setOnAction(event -> {
                    Resident resident = getTableView().getItems().get(getIndex());
                    handleDelete(resident);
                });

                pane.setSpacing(5);
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

    private void loadResidents() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/users"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Trạng thái của request " + response);
            if (response.statusCode() == 200) {
                User[] users = objectMapper.readValue(response.body(), User[].class);

                for (User user : users) {
                    System.out.println("Đang xử lý user: " + user.getUsername() + " (ID: " + user.getId() + ")");

                    // Tìm cư dân tương ứng với username
                    Resident resident = residentService.findResidentByUsername(user.getUsername());
                    System.out.println("Kết quả tìm resident: " + resident);

                    if (resident == null) {
                        System.out.println("Resident chưa tồn tại, tạo mới.");

                        // Nếu không có, tạo mới một Resident
                        resident = new Resident();
                        resident.setUser(user);  // Gán User cho Resident
                        resident.setTrangThaiXacThuc(XacThuc.CHUA_XAC_THUC);  // Đặt trạng thái xác thực là CHUA_XAC_THUC
                        resident.setTrangThaiTamVang(TamVangStatus.thuong_tru);  // Đặt trạng thái tạm vắng là thuong_tru

                        residentService.saveResident(resident); // Lưu vào DB
                    }

                    residentList.add(resident);
                }

                residentTable.setItems(residentList);
                thongBaoLabel.setText("Tải lên dữ liệu cư dân thành công.");
            } else {
                thongBaoLabel.setText("Tải lên dữ liệu cư dân thất bại.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi chi tiết
            thongBaoLabel.setText("Lỗi tải lên dữ liệu cư dân!");
        }
    }

    private String formatEnumToDisplayName(String enumName) {
        // Thay "_" bằng khoảng trắng
        String formattedName = enumName.replace("_", " ");

        // Sử dụng Matcher để chuyển chữ cái đầu tiên của mỗi từ thành chữ hoa
        Pattern pattern = Pattern.compile("\\b[a-z]");
        Matcher matcher = pattern.matcher(formattedName);

        // Thay đổi chữ cái đầu tiên của mỗi từ thành chữ hoa
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, matcher.group().toUpperCase());
        }
        matcher.appendTail(result);

        return result.toString();
    }


    private void openResidentStage(Resident resident, int mode) {
        try {
            System.out.println("Bắt đầu mở cửa sổ cư dân. Mode = " + mode);

            // Tạo FXMLLoader và nạp fxml view
            System.out.println("Đang tải file FXML: /view/resident/resident_edit.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/resident/resident_edit.fxml"));

            // Thiết lập controller factory sử dụng Spring context để quản lý bean
            System.out.println("Thiết lập controller factory từ Spring context");
            fxmlLoader.setControllerFactory(MainApplication.springContext::getBean);

            // Tạo Scene từ fxml đã load, kích thước của cửa sổ
            System.out.println("Đang load FXML và tạo Scene");
            Scene residentScene = new Scene(fxmlLoader.load(), 875, 415);

            // Tạo một cửa sổ mới cho chế độ chỉnh sửa cư dân
            Stage residentEditStage = new Stage();
            residentEditStage.initModality(Modality.APPLICATION_MODAL);

            // Thiết lập tiêu đề cửa sổ theo chế độ
            switch (mode) {
                case 1:
                    residentEditStage.setTitle("Chi tiết cư dân");
                    break;
                case 2:
                    residentEditStage.setTitle("Thêm mới cư dân");
                    break;
                case 3:
                    residentEditStage.setTitle("Cập nhật cư dân");
                    break;
                default:
                    residentEditStage.setTitle("Quản lý cư dân");
                    break;
            }
            System.out.println("Đã thiết lập tiêu đề cửa sổ: " + residentEditStage.getTitle());

            // Gắn Scene vào Stage
            residentEditStage.setScene(residentScene);
            System.out.println("Đã gắn Scene vào Stage");

            // Lấy controller của fxml đã load và thiết lập các giá trị cần thiết
            ResidentEditController residentEditController = fxmlLoader.getController();
            System.out.println("Đã lấy được controller: " + residentEditController.getClass().getName());

            residentEditController.setStage(residentEditStage);
            System.out.println("Đã thiết lập Stage cho controller");

            residentEditController.setResident(resident, mode);
            System.out.println("Đã thiết lập Resident và mode cho controller");

            // Hiển thị cửa sổ và chờ người dùng tương tác (modal)
            residentEditStage.showAndWait();
            System.out.println("Cửa sổ cư dân đã hiển thị xong");
        } catch (Exception e) {
            System.out.println("Đã xảy ra lỗi khi mở cửa sổ cư dân: " + e.getMessage());
            e.printStackTrace();
            Logger.getLogger(ResidentController.class.getName()).log(Level.SEVERE, "Lỗi khi mở cửa sổ cư dân", e);
        }
    }


    @FXML
    private void handleAdd() {
        try {
            openResidentStage(null, 2);
            Resident newResident = residentEditController.getResident();
            if (newResident != null) {
                residentList.add(newResident);
                residentTable.refresh();
                thongBaoLabel.setText("Thêm mới cư dân thành công!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit(Resident selectedResident) {
        if (selectedResident == null) {
            thongBaoLabel.setText("Chọn cư dân để cập nhật!");
            return;
        }
        openResidentStage(selectedResident, 3);
        Resident updatedResident = residentEditController.getResident();
        if (updatedResident != null && !updatedResident.equals(selectedResident)) {
            for (int i = 0; i < residentList.size(); i++) {
                if (residentList.get(i).getId().equals(updatedResident.getId())) {
                    residentList.set(i, updatedResident);
                    break;
                }
            }
            residentTable.refresh();
            thongBaoLabel.setText("Cập nhật cư dân thành công!");
        }
    }

    @FXML
    private void handleDelete(Resident selectedResident) {
        if (selectedResident == null) {
            thongBaoLabel.setText("Chọn cư dân để xóa!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc muốn xóa cư dân này?");
        alert.setContentText("Tên cư dân: " + selectedResident.getHoTen());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                residentService.deleteResident(selectedResident.getId());
                residentList.remove(selectedResident);
                residentTable.refresh();
                thongBaoLabel.setText("Xóa cư dân thành công!");
            } catch (Exception e) {
                e.printStackTrace();
                thongBaoLabel.setText("Lỗi khi xóa cư dân.");
            }
        }
    }


    @FXML
    private void handleViewDetails(Resident resident) {
        openResidentStage(resident, 1);
    }

    @FXML
    private void handleSearch() {
        // Kiểm tra giá trị từ ComboBox
        String filterRoom = roomComboBox.getValue();
        String filterAuthStatus = xacThucStatusSearchComboBox.getValue();
        String filterTamVangStatus = tamVangStatusSearchComboBox.getValue();

        System.out.println("== ComboBox Values ==");
        System.out.println("Room: " + filterRoom);
        System.out.println("Auth Status: " + filterAuthStatus);
        System.out.println("Tam Vang Status: " + filterTamVangStatus);

        // Tạo URL với query params
        String url = "http://localhost:8080/api/residents";
        List<String> queryParams = new ArrayList<>();

        if (filterRoom != null && !filterRoom.isEmpty()) {
            queryParams.add("so_phong_id=" + URLEncoder.encode(filterRoom, StandardCharsets.UTF_8));
        }
        if (filterAuthStatus != null && !filterAuthStatus.isEmpty()) {
            queryParams.add("trang_thai_xac_thuc=" + URLEncoder.encode(filterAuthStatus, StandardCharsets.UTF_8));
        }
        if (filterTamVangStatus != null && !filterTamVangStatus.isEmpty()) {
            queryParams.add("trang_thai_tam_vang=" + URLEncoder.encode(filterTamVangStatus, StandardCharsets.UTF_8));
        }

        if (!queryParams.isEmpty()) {
            url += "?" + String.join("&", queryParams);
        }

        System.out.println("== Request URL ==");
        System.out.println(url);

        try {
            // Tạo HttpClient nếu chưa có
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
            System.out.println(response.statusCode());

            System.out.println("== Raw JSON Response ==");
            System.out.println(response.body());

            // Parse JSON (mặc định là mảng Resident[])
            Resident[] filteredResidents = objectMapper.readValue(response.body(), Resident[].class);

            System.out.println("== Parsed Resident Count ==");
            System.out.println(filteredResidents.length);

            // In danh sách cư dân filter được
            System.out.println("== Filtered Residents ==");
            for (Resident resident : filteredResidents) {
                System.out.println(resident);
            }

            // Cập nhật danh sách
            residentList.setAll(filteredResidents);
            residentTable.setItems(residentList);

        } catch (Exception e) {
            System.out.println("== Exception Occurred ==");
            e.printStackTrace();
        }
    }



    @FXML
    private void handleResetSearch() {
        roomComboBox.setValue(null);
        tamVangStatusSearchComboBox.setValue(null);
        xacThucStatusSearchComboBox.setValue(null);
        loadResidents();
    }
}