package controllers;

import controllers.LoginController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import models.Report;
import models.ReportResponse;
import models.enums.ReportStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import services.ReportService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class UserReportHistoryController {

    @FXML private TableView<Report> notificationTable;
    @FXML private TableColumn<Report, Long> idColumn;
    @FXML private TableColumn<Report, String> createdAtColumn;
    @FXML private TableColumn<Report, String> tilteColumn;
    @FXML private TableColumn<Report, ReportStatus> stateColumn;
    @FXML private TableColumn<Report, Void> detailColumn;

    @Autowired
    private ReportService reportService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTableColumns();
        loadReports();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        createdAtColumn.setCellValueFactory(cellData ->
                cellData.getValue().createdAtProperty().concat(formatter)
        );

        tilteColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        setupActionColumn();
    }

    private void setupActionColumn() {
        detailColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Report, Void> call(final TableColumn<Report, Void> param) {
                return new TableCell<>() {
                    private final HBox container = new HBox(10);
                    private final javafx.scene.control.Button viewBtn = new javafx.scene.control.Button("Xem");
                    private final javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button("Xóa");

                    {
                        viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                        viewBtn.setOnAction(event -> showReportDetails(getTableView().getItems().get(getIndex())));
                        deleteBtn.setOnAction(event -> handleDeleteReport(getTableView().getItems().get(getIndex())));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            container.getChildren().clear();
                            container.getChildren().addAll(viewBtn, deleteBtn);
                            container.setAlignment(Pos.CENTER);
                            setGraphic(container);
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            setAlignment(Pos.CENTER);
                        }
                    }
                };
            }
        });
    }

    private void showReportDetails(Report report) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết khiếu nại");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefSize(500, 400);  // Tăng kích thước tổng thể của dialog

        Label titleLabel = new Label("Tiêu đề: " + report.getTitle());

        Label descLabel = new Label("Nội dung:");
        TextArea descArea = new TextArea(report.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(15);  // Tăng số dòng hiển thị
        descArea.setPrefHeight(120);

        Label createdAtLabel = new Label("Thời gian tạo: " + report.getCreatedAt().format(formatter));
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

                String responseInfo = String.format("%s - %s",
                        shortText,
                        response.getRespondedAt().format(formatter));

                responses.add(responseInfo);
                responseMap.put(responseInfo, response);
            }
        }

        responsesList.setItems(responses);
        responsesList.setPrefHeight(120);  // Đặt chiều cao cố định cho ListView

        // Cho phép nhấp đúp để xem chi tiết phản hồi
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
                titleLabel,
                descLabel, descArea,
                createdAtLabel,
                statusLabel,
                new Label("Lịch sử phản hồi:"), hintLabel, responsesList
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.setResizable(true);  // Cho phép thay đổi kích thước
        dialog.showAndWait();
    }


    private void showResponseDetail(ReportResponse response) {
        Dialog<Void> detailDialog = new Dialog<>();
        detailDialog.setTitle("Chi tiết phản hồi");
        detailDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.setPrefSize(500, 400);

        Label timeLabel = new Label("Thời gian phản hồi: " + response.getRespondedAt());
        Label textLabel = new Label("Nội dung phản hồi:");
        TextArea textArea = new TextArea(response.getResponseText());
        textArea.setWrapText(true);
        textArea.setEditable(false);

        content.getChildren().addAll(timeLabel, textLabel, textArea);
        detailDialog.getDialogPane().setContent(content);
        detailDialog.setResizable(false);
        detailDialog.showAndWait();
    }


    private void handleDeleteReport(Report report) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận xóa");
        confirmation.setHeaderText("Bạn chắc chắn muốn xóa khiếu nại này?");
        confirmation.setContentText("Thao tác này không thể hoàn tác!");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                reportService.deleteReport(report.getId());
                loadReports(); // Refresh table
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Lỗi khi xóa: " + e.getMessage()).show();
            }
        }
    }

    void loadReports() {
        ObservableList<Report> reports = FXCollections.observableArrayList(
                reportService.getAllReportsByUser(LoginController.getLoggedInUser().getId())
        );
        notificationTable.setItems(reports);
    }
}