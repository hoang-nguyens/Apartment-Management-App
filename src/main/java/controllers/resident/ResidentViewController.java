package controllers.resident;

import app.MainApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import models.resident.Resident;
import models.user.User;
import models.enums.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import services.resident.ResidentService;
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
    private boolean isDataLoaded = false;
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
            } else {
                thongBaoLabel.setText("Không tìm thấy thông tin cư dân.");
            }
            return;
        }
        loadResidents();
        System.out.println("ResidentViewController initialized");
        roomComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(SoPhong.values())
                        .map(Enum::name)
                        .collect(Collectors.toList())
        ));
        xacThucStatusSearchComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(XacThuc.values())
                        .map(Enum::name)
                        .collect(Collectors.toList())
        ));
        tamVangStatusSearchComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(TamVangStatus.values())
                        .map(Enum::name)
                        .collect(Collectors.toList())
        ));
        roomComboBox.setOnAction(event -> handleSearch());
        xacThucStatusSearchComboBox.setOnAction(event -> handleSearch());
        tamVangStatusSearchComboBox.setOnAction(event -> handleSearch());
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUser().getUsername()));
        hotenColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoTen()));
        cccdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCccd()));
        sdtColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSdt()));
        genderColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getGioiTinh()));
        apartmentNumberColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSoPhong()));
        xacThucStatusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTrangThaiXacThuc()));
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("Xem");
            private final Button editButton = new Button("Sửa");
            private final Button deleteButton = new Button("Xóa");
            {
                viewButton.setStyle("-fx-background-color: #1E90FF; -fx-text-fill: white;");   // Xanh nước biển
                editButton.setStyle("-fx-background-color: #32CD32; -fx-text-fill: white;");   // Xanh lá cây
                deleteButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white;"); // Đỏ
            }
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
        // Chỉ bỏ qua tải dữ liệu nếu đã tải và không cần làm mới
        if (isDataLoaded) {
            residentTable.setItems(residentList);
            return;
        }

        try {
            // Gọi API để lấy dữ liệu mới nhất
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/users"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Xóa dữ liệu cũ và cập nhật danh sách mới
                residentList.clear();
                User[] users = objectMapper.readValue(response.body(), User[].class);
                for (User user : users) {
                    Resident resident = residentService.findResidentByUsername(user.getUsername());
                    if (resident == null) {
                        resident = new Resident();
                        resident.setUser(user);
                        resident.setTrangThaiXacThuc(XacThuc.CHUA_XAC_THUC);
                        resident.setTrangThaiTamVang(TamVangStatus.thuong_tru);
                        residentService.saveResident(resident);
                    }
                    residentList.add(resident);
                }
                residentTable.setItems(residentList);
                isDataLoaded = true;
                thongBaoLabel.setText("Tải dữ liệu thành công.");
            } else {
                thongBaoLabel.setText("Lỗi khi tải dữ liệu.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            thongBaoLabel.setText("Lỗi kết nối server!");
        }
    }

    private String formatEnumToDisplayName(String enumName) {
        String formattedName = enumName.replace("_", " ");
        Pattern pattern = Pattern.compile("\\b[a-z]");
        Matcher matcher = pattern.matcher(formattedName);
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
            System.out.println("Đang tải file FXML: /view/resident/resident_edit.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/resident/resident_edit.fxml"));
            System.out.println("Thiết lập controller factory từ Spring context");
            fxmlLoader.setControllerFactory(MainApplication.springContext::getBean);
            System.out.println("Đang load FXML và tạo Scene");
            Scene residentScene = new Scene(fxmlLoader.load(), 875, 415);
            Stage residentEditStage = new Stage();
            residentEditStage.initModality(Modality.APPLICATION_MODAL);
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
            residentEditStage.setScene(residentScene);
            System.out.println("Đã gắn Scene vào Stage");
            ResidentEditController residentEditController = fxmlLoader.getController();
            System.out.println("Đã lấy được controller: " + residentEditController.getClass().getName());
            residentEditController.setStage(residentEditStage);
            System.out.println("Đã thiết lập Stage cho controller");
            residentEditController.setResident(resident, mode);
            System.out.println("Đã thiết lập Resident và mode cho controller");
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
        //needsRefresh = true;
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
        //needsRefresh = true;
    }

    @FXML
    private void handleViewDetails(Resident resident) {
        openResidentStage(resident, 1);
    }

    @FXML
    private void handleSearch() {
        String filterRoom = roomComboBox.getValue();
        String filterAuthStatus = xacThucStatusSearchComboBox.getValue();
        String filterTamVangStatus = tamVangStatusSearchComboBox.getValue();
        System.out.println("== ComboBox Values ==");
        System.out.println("Room: " + filterRoom);
        System.out.println("Auth Status: " + filterAuthStatus);
        System.out.println("Tam Vang Status: " + filterTamVangStatus);
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
            Resident[] filteredResidents = objectMapper.readValue(response.body(), Resident[].class);
            System.out.println("== Parsed Resident Count ==");
            System.out.println(filteredResidents.length);
            System.out.println("== Filtered Residents ==");
            for (Resident resident : filteredResidents) {
                System.out.println(resident);
            }
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