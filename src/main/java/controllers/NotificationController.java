package controllers;

import controllers.LoginController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Resident;
import models.User;
import models.enums.NotificationType;
import models.enums.SoPhong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.NotificationService;
import services.ResidentService;
import services.UserService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private List<SoPhong> selectedRooms = new ArrayList<>();
    @FXML private ComboBox<String> typeComboBox;
    @FXML private RadioButton allRadio;
    @FXML private RadioButton manualRadio;
    @FXML private Button chonPhongButton;
    @FXML private TextField titleField;
    @FXML private TextArea messageArea;
    @FXML private Label stateLabel;
    @FXML private Button sentNotificationButton;
    private UserService userService;
    private ResidentService residentService;
    @Autowired
    private NotificationHistoryController notificationHistoryController;

    public void initialize() {
        // Bind nút "Chọn phòng" với trạng thái RadioButton
        chonPhongButton.disableProperty().bind(
                manualRadio.selectedProperty().not()
        );
    }

    @FXML
    public void handleSendNotification() {
        String typeString = typeComboBox.getValue();
        String title = titleField.getText().trim();
        String message = messageArea.getText().trim();

        // Validate input
        if (typeString == null || title.isEmpty() || message.isEmpty()) {
            stateLabel.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        // Convert notification type
        NotificationType type;
        try {
            type = NotificationType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            stateLabel.setText("Loại thông báo không hợp lệ.");
            return;
        }

        List<Long> userIds;
        if (allRadio.isSelected()) {
            // Gửi cho tất cả người dùng
            List<User> allUsers = userService.getAllUsersWithUserRole();
            userIds = allUsers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
        } else {
            // Xử lý chọn thủ công theo phòng
            if (selectedRooms.isEmpty()) {
                stateLabel.setText("Vui lòng chọn ít nhất một phòng!");
                return;
            }

            // Lấy user IDs từ các phòng đã chọn
            userIds = new ArrayList<>();
            for (SoPhong room : selectedRooms) {
                List<Resident> residents = residentService.getResidentsByRoom(room);
                residents.stream()
                        .map(Resident::getUser)
                        .map(User::getId)
                        .forEach(userIds::add);
            }

            // Loại bỏ trùng lặp
            userIds = userIds.stream()
                    .distinct()
                    .collect(Collectors.toList());

            if (userIds.isEmpty()) {
                stateLabel.setText("Không tìm thấy người dùng trong các phòng đã chọn!");
                return;
            }
        }

        // Tạo request
        NotificationRequest request = new NotificationRequest();
        request.setTitle(title);
        request.setMessage(message);
        request.setType(type.name());
        request.setUserIds(userIds);

        // Lấy thông tin người gửi
        User loggedInUser = LoginController.getLoggedInUser();
        if (loggedInUser == null) {
            stateLabel.setText("Lỗi xác thực người dùng!");
            return;
        }
        request.setCreatedBy(loggedInUser.getId());

        // Gửi thông báo
        try {
            notificationService.sendNotificationToUsers(
                    request.getTitle(),
                    request.getMessage(),
                    type,
                    request.getCreatedBy(),
                    request.getUserIds()
            );
            notificationHistoryController.loadNotificationHistory();
            stateLabel.setText("Gửi thành công!");
            selectedRooms.clear(); // Reset danh sách phòng đã chọn
        } catch (Exception e) {
            e.printStackTrace();
            stateLabel.setText("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @FXML
    private void handleChooseRooms() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chọn phòng");
        alert.setHeaderText("Chọn các phòng muốn gửi thông báo");
        alert.getDialogPane().setPrefSize(850, 600); // Kích thước cố định

        // Tạo ScrollPane chứa các hàng
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(500);

        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(15));

        final int ITEMS_PER_ROW = 4;
        List<SoPhong> allRooms = new ArrayList<>(List.of(SoPhong.values()));

        // Tạo bản đồ checkbox để quản lý trạng thái
        Map<SoPhong, CheckBox> checkBoxMap = new HashMap<>();

        // Tạo các hàng với checkbox và nút chức năng
        for (int i = 0; i < allRooms.size(); i += ITEMS_PER_ROW) {
            List<SoPhong> roomGroup = allRooms.subList(i, Math.min(i + ITEMS_PER_ROW, allRooms.size()));

            HBox rowBox = new HBox(10);
            rowBox.setAlignment(Pos.CENTER_LEFT);

            List<CheckBox> currentRowCheckboxes = new ArrayList<>();
            for (SoPhong room : roomGroup) {
                CheckBox cb = new CheckBox(room.name());
                cb.setSelected(selectedRooms.contains(room)); // Khôi phục trạng thái trước đó
                checkBoxMap.put(room, cb);
                currentRowCheckboxes.add(cb);
                rowBox.getChildren().add(cb);
            }

            // Tạo container cho các nút chức năng
            HBox buttonContainer = new HBox(5);

            // Nút chọn cả hàng
            Button selectRowButton = new Button("✓ Chọn hàng");
            selectRowButton.setStyle("-fx-background-color: #e8f5e9;");
            selectRowButton.setOnAction(e ->
                    currentRowCheckboxes.forEach(cb -> cb.setSelected(true))
            );

            // Nút hủy chọn hàng
            Button deselectRowButton = new Button("✗ Hủy hàng");
            deselectRowButton.setStyle("-fx-background-color: #ffebee;");
            deselectRowButton.setOnAction(e ->
                    currentRowCheckboxes.forEach(cb -> cb.setSelected(false))
            );

            buttonContainer.getChildren().addAll(selectRowButton, deselectRowButton);
            rowBox.getChildren().add(buttonContainer);
            mainContainer.getChildren().add(rowBox);
        }

        scrollPane.setContent(mainContainer);
        alert.getDialogPane().setContent(scrollPane);

        // Xử lý kết quả
        ButtonType confirmButton = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(confirmButton, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            selectedRooms.clear();

            // Cập nhật danh sách phòng đã chọn
            checkBoxMap.forEach((room, cb) -> {
                if (cb.isSelected()) {
                    selectedRooms.add(room);
                }
            });

            stateLabel.setText("Đã chọn " + selectedRooms.size() + " phòng");
        }
    }

    public static class NotificationRequest {
        private String title;
        private String message;
        private String type;
        private Long createdBy;
        private List<Long> userIds;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(Long createdBy) {
            this.createdBy = createdBy;
        }

        public List<Long> getUserIds() {
            return userIds;
        }

        public void setUserIds(List<Long> usersId) {
            this.userIds = usersId;
        }
    }

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService, UserService userService, ResidentService residentService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.residentService = residentService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest req) {
        NotificationType type;

        try {
            type = NotificationType.valueOf(req.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid notification type: " + req.getType());
        }

        notificationService.sendNotificationToUsers(
                req.getTitle(),
                req.getMessage(),
                type,
                req.getCreatedBy(),
                req.getUserIds()
        );

        return ResponseEntity.ok("Notification sent!");
    }


    @PostMapping("/read/{id}")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Marked as read!");
    }

    @GetMapping("/unread/{userId}")
    public ResponseEntity<?> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }
}