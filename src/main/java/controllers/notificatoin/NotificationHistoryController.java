package controllers.notificatoin;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import models.notification.Notification;
import models.user.User;
import models.enums.NotificationType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import services.notification.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class NotificationHistoryController {

    @FXML
    private TableView<Notification> notificationTable;

    @FXML
    private TableColumn<Notification, Long> idColumn;

    @FXML
    private TableColumn<Notification, NotificationType> typeColumn;

    @FXML
    private TableColumn<Notification, LocalDateTime> createdAtColumn;

    @FXML
    private  TableColumn<Notification, String> creatorColumn;

    @FXML
    private TableColumn<Notification, Void> detailColumn;
    private boolean isDataLoaded = false;
    ObservableList<Notification> observableList = FXCollections.observableArrayList();
    private final NotificationService notificationService;

    @Autowired
    public NotificationHistoryController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadNotificationHistory();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(NotificationType type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                } else {
                    setText(formatNotificationType(type));
                }
            }
        });

        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (empty || dateTime == null) {
                    setText(null);
                } else {
                    setText(dateTime.format(formatter));
                }
            }
        });
        creatorColumn.setCellValueFactory(cellData -> {
            User creator = cellData.getValue().getCreatedBy();
            String username = (creator != null) ? creator.getUsername() : "Không xác định";
            return new SimpleStringProperty(username);
        });

        detailColumn.setCellFactory(createDetailButtonCellFactory());
    }

    private String formatNotificationType(NotificationType type) {
        return switch (type) {
            case PAYMENT -> "PAYMENT";
            case OVERDUE -> "OVERDUE";
            case ACTIVITY -> "ACTIVITY";
            case APP -> "APP";
        };
    }

    private Callback<TableColumn<Notification, Void>, TableCell<Notification, Void>> createDetailButtonCellFactory() {
        return param -> new TableCell<>() {
            private final Button detailBtn = new Button("Xem");
            private final Button deleteBtn = new Button("Xóa");
            private final HBox buttonBox = new HBox(12);

            {
                detailBtn.setStyle("-fx-background-color: #7A9EE6; -fx-text-fill: white;");
                detailBtn.setOnAction(event -> {
                    Notification notification = getTableView().getItems().get(getIndex());
                    showNotificationDetail(notification);
                });

                // Style nút xóa
                deleteBtn.setStyle("-fx-background-color: #E67A7A; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    Notification notification = getTableView().getItems().get(getIndex());
                    boolean confirmed = showConfirmationDialog("Xác nhận xóa", "Bạn có chắc muốn xóa thông báo này?");
                    if (confirmed) {
                        notificationService.deleteNotification(notification.getId());
                        notificationTable.getItems().remove(notification);
                        notificationTable.refresh();
                    }
                });
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.getChildren().addAll(detailBtn, deleteBtn);
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        };
    }

    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType okButton = new ButtonType("Xóa", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, cancelButton);

        return alert.showAndWait().filter(response -> response == okButton).isPresent();
    }

    protected void loadNotificationHistory() {
        if(!isDataLoaded) {
            List<Notification> notifications = notificationService.getNotificationsCreatedByAdmins();
            observableList = FXCollections.observableArrayList(notifications);
        }
        notificationTable.setItems(observableList);
    }

    private void showNotificationDetail(Notification notification) {
        try {
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

            // Thêm thông tin người tạo
            gridPane.add(new Label("Người tạo:"), 0, 0);
            gridPane.add(new Label(notification.getCreatedBy() != null ?
                    notification.getCreatedBy().getUsername(): "System"), 1, 0);

            gridPane.add(new Label("Loại:"), 0, 1);
            gridPane.add(new Label(formatNotificationType(notification.getType())), 1, 1);

            gridPane.add(new Label("Ngày tạo:"), 0, 2);
            gridPane.add(new Label(notification.getCreatedAt().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))), 1, 2);

            gridPane.add(new Label("Nội dung:"), 0, 3);
            gridPane.add(textArea, 0, 4, 2, 1);

            alert.getDialogPane().setContent(gridPane);
            alert.getDialogPane().setPrefSize(600, 400);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Các phương thức REST API
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        return notificationService.getNotificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}