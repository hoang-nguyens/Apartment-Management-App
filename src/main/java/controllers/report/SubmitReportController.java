package controllers.report;

import controllers.login_signup.LoginController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import services.report.ReportService;

@Controller
public class SubmitReportController {

    @Autowired
    private ReportService reportService;
    @Autowired
    UserReportHistoryController userReportHistoryController ;
    @Autowired
    AdminReportListController adminReportListController;

    @FXML private TextField titleField;
    @FXML private TextArea messageArea;
    @FXML private Label stateLabel;

    @FXML
    private void handleSendReport() {
        String title = titleField.getText().trim();
        String description = messageArea.getText().trim();
        User user = LoginController.getLoggedInUser();

        // Validate input
        if (title.isEmpty() || description.isEmpty()) {
            showError("Vui lòng điền đầy đủ tiêu đề và nội dung!");
            return;
        }

        if (user == null) {
            showError("Vui lòng đăng nhập để gửi khiếu nại");
            return;
        }

        // Create background task
        Task<Void> submitTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                reportService.submitReport(user, title, description);
                return null;
            }
        };

        // Handle task completion
        submitTask.setOnSucceeded(e -> Platform.runLater(() -> {
            titleField.clear();
            messageArea.clear();
            showSuccess("Gửi khiếu nại thành công!");
            userReportHistoryController.loadReports();
            adminReportListController.setDataLoaded();
        }));

        submitTask.setOnFailed(e -> Platform.runLater(() ->
                showError("Lỗi: " + submitTask.getException().getMessage())
        ));

        new Thread(submitTask).start();
    }

    private void showError(String message) {
        stateLabel.setText(message);
        stateLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        stateLabel.setText(message);
        stateLabel.setStyle("-fx-text-fill: green;");
    }
}