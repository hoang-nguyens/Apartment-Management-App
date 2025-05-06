package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Notification;
import models.User;
import models.enums.NotificationType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.NotificationService;
import services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private RadioButton allRadio;
    @FXML private RadioButton manualRadio;
    @FXML private TextField titleField;
    @FXML private TextArea messageArea;
    @FXML private Label stateLabel;
    @FXML private Button sentNotificationButton;
    private UserService userService;
    @FXML
    public void handleSendNotification() {
        String typeString = typeComboBox.getValue();
        String title = titleField.getText().trim();
        String message = messageArea.getText().trim();

        if (typeString == null || title.isEmpty() || message.isEmpty()) {
            stateLabel.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        NotificationType type;
        try {
            type = NotificationType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            stateLabel.setText("Loại thông báo không hợp lệ.");
            return;
        }

        List<Long> userIds;
        if (allRadio.isSelected()) {
            List<User> allUsers = userService.getAllUsersWithUserRole();
            userIds = allUsers.stream().map(User::getId).collect(Collectors.toList());
        } else {
            // TODO: Xử lý chọn thủ công (ví dụ từ danh sách TableView)
            stateLabel.setText("Chức năng chọn thủ công chưa được hỗ trợ.");
            return;
        }

        NotificationRequest request = new NotificationRequest();
        request.setTitle(title);
        request.setMessage(message);
        request.setType(type.name());
        request.setUserIds(userIds);
        User loggedInUser = LoginController.getLoggedInUser();
        request.setCreatedBy(loggedInUser.getId()); // Hoặc người dùng đăng nhập hiện tại

        try {
            notificationService.sendNotificationToUsers(
                    request.getTitle(),
                    request.getMessage(),
                    NotificationType.valueOf(request.getType().toUpperCase()),
                    request.getCreatedBy(),
                    request.getUserIds()
            );
            stateLabel.setText("Gửi thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            stateLabel.setText("Lỗi khi gửi thông báo.");
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

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
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
