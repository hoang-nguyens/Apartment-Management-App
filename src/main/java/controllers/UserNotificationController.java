package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import models.Notification;
import models.User;
import models.UserNotification;
import models.enums.NotificationType;
import org.springframework.stereotype.Controller;
import services.NotificationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class UserNotificationController {

    @FXML
    private ListView<UserNotification> notificationList;
    @FXML
    private ToggleButton newToggle;
    @FXML
    private ToggleButton readToggle;

    @FXML
    private Label updateLabel;
    private final NotificationService notificationService;
    private User currentUser;
    private ObservableList<UserNotification> unreadNotifications;
    private ObservableList<UserNotification> readNotifications;

    public UserNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadNotifications();
    }

    @FXML
    public void initialize() {
        // Set up toggle group for the buttons
        ToggleGroup toggleGroup = new ToggleGroup();
        newToggle.setToggleGroup(toggleGroup);
        readToggle.setToggleGroup(toggleGroup);
        newToggle.setSelected(true);
        newToggle.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        User loggedInUser = LoginController.getLoggedInUser();
        setCurrentUser(loggedInUser);
        // Set up cell factory for the list view
        notificationList.setCellFactory(new Callback<ListView<UserNotification>, ListCell<UserNotification>>() {
            @Override
            public ListCell<UserNotification> call(ListView<UserNotification> param) {
                return new ListCell<UserNotification>() {
                    @Override
                    protected void updateItem(UserNotification item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null || item.getNotification() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Notification notification = item.getNotification();
                            String text = String.format("[%s] %s - %s",
                                    formatNotificationType(notification.getType()),
                                    notification.getTitle(),
                                    notification.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                            setText(text);
                        }
                    }
                };
            }
        });

        // Handle item click to show details
        notificationList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double click
                UserNotification selected = notificationList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showNotificationDetail(selected);
                    if (!selected.getIsRead()) {
                        notificationService.markAsRead(selected.getId());
                        selected.setIsRead(true);
                        selected.setReadAt(LocalDateTime.now());
                        // Update lists
                        unreadNotifications.remove(selected);
                        updateLabel.setText(
                                String.format("Thông báo \"%s\" đã được chuyển sang mục đã đọc",
                                        selected.getNotification().getTitle())
                        );
                        readNotifications.add(selected);
                    }
                }
            }
        });

        // Handle toggle button changes
        newToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                notificationList.setItems(unreadNotifications);
                newToggle.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                readToggle.setStyle("");
            }
        });

        readToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                notificationList.setItems(readNotifications);
                readToggle.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                newToggle.setStyle("");
                updateLabel.setText("");
            }
        });
    }

    private void loadNotifications() {
        if (currentUser != null) {
            try {
                List<UserNotification> unread = notificationService.getUnreadNotifications(currentUser.getId());
                List<UserNotification> read = notificationService.getReadNotifications(currentUser.getId());

                unreadNotifications = FXCollections.observableArrayList(unread);
                readNotifications = FXCollections.observableArrayList(read);

                // Mặc định hiển thị thông báo chưa đọc
                notificationList.setItems(unreadNotifications);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể tải thông báo", Alert.AlertType.ERROR);
            }
        }
    }
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showNotificationDetail(UserNotification userNotification) {
        try {
            Notification notification = userNotification.getNotification();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chi tiết thông báo");
            alert.setHeaderText(notification.getTitle());

            TextArea textArea = new TextArea(notification.getMessage());
            textArea.setEditable(false);
            textArea.setWrapText(true);

            GridPane gridPane = new GridPane();
            gridPane.setMaxWidth(Double.MAX_VALUE);
            gridPane.setHgap(10);
            gridPane.setVgap(10);
            gridPane.setPadding(new Insets(10));

            gridPane.add(new Label("Loại:"), 0, 0);
            gridPane.add(new Label(formatNotificationType(notification.getType())), 1, 0);

            gridPane.add(new Label("Ngày tạo:"), 0, 1);
            gridPane.add(new Label(notification.getCreatedAt().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))), 1, 1);

            gridPane.add(new Label("Nội dung:"), 0, 2);
            gridPane.add(textArea, 0, 3, 2, 1);

            alert.getDialogPane().setContent(gridPane);
            alert.getDialogPane().setPrefSize(600, 400);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatNotificationType(NotificationType type) {
        return switch (type) {
            case PAYMENT -> "Thanh toán";
            case OVERDUE -> "Quá hạn";
            case ACTIVITY -> "Hoạt động";
            case APP -> "Ứng dụng";
        };
    }
}