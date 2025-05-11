package controllers.report;

import controllers.login_signup.LoginController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets; // Cho việc thiết lập padding
import javafx.scene.control.Label; // Nếu chưa có
import javafx.scene.control.ListView; // Cho danh sách phản hồi
import models.report.Report;
import models.report.ReportResponse;
import models.user.User;
import models.enums.ReportStatus;
import org.springframework.stereotype.Controller;
import services.report.ReportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class AdminReportListController {

    private boolean isDataLoaded = false;
    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private TableView<Report> reportTable;

    @FXML
    private TableColumn<Report, Long> idColumn;
    @FXML
    private TableColumn<Report, String> creatorNameColumn;
    @FXML
    private TableColumn<Report, String> roomColumn;
    @FXML
    private TableColumn<Report, String> titleColumn;
    @FXML
    private TableColumn<Report, ReportStatus> statusColumn;
    @FXML
    private TableColumn<Report, LocalDateTime> createdAtColumn;
    @FXML
    private TableColumn<Report, Void> actionColumn;

    private final ReportService reportService;

    private List<Report> allReports;


    public AdminReportListController(ReportService reportService) {
        this.reportService = reportService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupStatusFilter();
        loadAllReports();
        isDataLoaded = true;
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        creatorNameColumn.setCellValueFactory(cellData -> {
            Report report = cellData.getValue();
            String hoTen = "Không xác định";

            if (report.getUser() != null && report.getUser().getResident() != null) {
                hoTen = report.getUser().getResident().getHoTen();
            }
            return new SimpleStringProperty(hoTen);
        });

        // Sửa lại cách lấy số phòng
        roomColumn.setCellValueFactory(cellData -> {
            Report report = cellData.getValue();
            String soPhong = "Không xác định";

            if (report.getUser() != null && report.getUser().getResident() != null
                    && report.getUser().getResident().getSoPhong() != null) {
                soPhong = report.getUser().getResident().getSoPhong().toString();
            }
            return new SimpleStringProperty(soPhong);
        });
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        createdAtColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getCreatedAt())
        );

        createdAtColumn.setCellFactory(column -> new TableCell<Report, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item)); // Định dạng ngày
                }
            }
        });
        addActionButtonsToTable();
    }

    private void setupStatusFilter() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList("TẤT CẢ", "PENDING", "RESPONDED"));
        statusFilterComboBox.setValue("TẤT CẢ");

        statusFilterComboBox.setOnAction(event -> filterReports());
    }

    private void loadAllReports() {
        if(!isDataLoaded) {
            allReports = reportService.getAllReports();
            allReports.sort((r1, r2) -> Long.compare(r2.getId(), r1.getId()));
        }
        reportTable.setItems(FXCollections.observableArrayList(allReports));
    }

    private void filterReports() {
        String selected = statusFilterComboBox.getValue();
        List<Report> filtered;

        if ("PENDING".equals(selected)) {
            filtered = allReports.stream()
                    .filter(r -> r.getStatus() == ReportStatus.PENDING)
                    .collect(Collectors.toList());
        } else if ("RESPONDED".equals(selected)) {
            filtered = allReports.stream()
                    .filter(r -> r.getStatus() == ReportStatus.RESPONDED)
                    .collect(Collectors.toList());
        } else {
            filtered = allReports;
        }

        reportTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void addActionButtonsToTable() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("Xem");
            private final Button replyButton = new Button("Trả lời");

            {
                viewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                replyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                viewButton.setOnAction(event -> {
                    Report report = getTableView().getItems().get(getIndex());
                    handleView(report);
                });

                replyButton.setOnAction(event -> {
                    Report report = getTableView().getItems().get(getIndex());
                    handleReply(report);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10, viewButton, replyButton);
                    box.setAlignment(Pos.CENTER);
                    box.setPadding(new Insets(5));
                    setGraphic(box);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }
    private static final double DIALOG_WIDTH = 500;
    private static final double DIALOG_HEIGHT = 400;

    private void handleView(Report report) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết khiếu nại");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);

        Label titleLabel = new Label("Tiêu đề: " + report.getTitle());
        Label descLabel = new Label("Nội dung:");
        TextArea descArea = new TextArea(report.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(15);
        descArea.setMaxHeight(120);
        Label createdAtLabel = new Label("Ngày tạo: " + report.getCreatedAt());
        Label statusLabel = new Label("Trạng thái: " + report.getStatus());
        Label hintLabel = new Label("Click 2 lần để xem chi tiết");
        hintLabel.setStyle("-fx-text-fill: gray;");
        ListView<String> responsesList = new ListView<>();
        ObservableList<String> responses = FXCollections.observableArrayList();

        Map<String, ReportResponse> responseMap = new HashMap<>();

        if (report.getResponses() != null) {
            for (ReportResponse response : report.getResponses()) {
                String shortText = response.getResponseText();
                if (shortText.length() > 40) {  // Hiển thị nhiều text hơn
                    shortText = shortText.substring(0, 40) + "...";
                }
                String responseInfo = String.format("%s - %s - %s",
                        shortText,
                        response.getAdmin().getUsername(),
                        response.getRespondedAt());
                responses.add(responseInfo);
                responseMap.put(responseInfo, response);
            }
        }

        responsesList.setItems(responses);
        responsesList.setPrefHeight(120);
        // Thêm sự kiện click 2 lần để xem chi tiết phản hồi
        responsesList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = responsesList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    ReportResponse response = responseMap.get(selected);
                    showResponseDetail(response);
                }
            }
        });

        content.getChildren().addAll(
                titleLabel, descLabel, descArea, createdAtLabel,
                statusLabel, new Label("Lịch sử phản hồi:"), responsesList
        );

        dialog.getDialogPane().setContent(content);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    private void showResponseDetail(ReportResponse response) {
        Dialog<Void> detailDialog = new Dialog<>();
        detailDialog.setTitle("Chi tiết phản hồi");
        detailDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);

        Label adminLabel = new Label("Người phản hồi: " + response.getAdmin().getUsername());
        Label timeLabel = new Label("Thời gian phản hồi: " + response.getRespondedAt());
        Label textLabel = new Label("Nội dung phản hồi:");
        TextArea textArea = new TextArea(response.getResponseText());
        textArea.setWrapText(true);
        textArea.setEditable(false);

        content.getChildren().addAll(adminLabel, timeLabel, textLabel, textArea);
        detailDialog.getDialogPane().setContent(content);
        detailDialog.setResizable(false);
        detailDialog.showAndWait();
    }

    private void handleReply(Report report) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Phản hồi báo cáo");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);

        Label titleLabel = new Label("Tiêu đề: " + report.getTitle());
        Label descLabel = new Label("Nội dung: " );
        TextArea descArea = new TextArea(report.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(15);
        descArea.setMaxHeight(120);
        Label createdAtLabel = new Label("Ngày tạo: " + report.getCreatedAt());
        Label statusLabel = new Label("Trạng thái: " + report.getStatus());

        TextArea responseField = new TextArea();
        responseField.setPromptText("Nhập nội dung phản hồi...");
        responseField.setWrapText(true);
        responseField.setPrefHeight(120);

        content.getChildren().addAll(
                titleLabel, descLabel,descArea, createdAtLabel,
                statusLabel, new Label("Nội dung phản hồi:"), responseField
        );

        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String responseText = responseField.getText().trim();
            if (!responseText.isEmpty()) {
                User currentAdmin = LoginController.getLoggedInUser();

                try {
                    reportService.respondToReport(
                            report.getId(),
                            currentAdmin,
                            responseText
                    );
                    setDataLoaded();
                    loadAllReports();
                    showAlert("Thành công", "Đã gửi phản hồi!", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Lỗi", "Không thể gửi phản hồi!", Alert.AlertType.ERROR);
                }
            }
        }
    }

    void setDataLoaded(){
        isDataLoaded = false;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}