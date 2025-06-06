package controllers.home;

import controllers.login_signup.LoginController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import models.user.User;
import org.springframework.stereotype.Controller;
import utils.UserUtils;

import java.io.IOException;
import java.util.List;

import static app.MainApplication.springContext;

@Controller
public class HomeController {
    @FXML private Button homeButton;
    @FXML private Button residentButton;
    @FXML private Button feeButton;
    @FXML private Button billButton;
    @FXML private Button reportButton;
    @FXML private Button supportButton;
    @FXML private Button notificationButton;

    private List<Button> buttons; // Danh sách các button

    @FXML
    public void initialize() {
        buttons = List.of(homeButton, residentButton, feeButton, billButton, reportButton, supportButton);
        User loggedInUser = LoginController.getLoggedInUser(); // Lấy thông tin user từ LoginController
        if (loggedInUser != null) {
            usernameItem.setText("Tài khoản: " + loggedInUser.getUsername());
        }
        onManageHome();
    }

    private void handleButtonClick(Button clickedButton) {
        // Đặt tất cả button thành "other_button"
        for (Button button : buttons) {
            button.getStyleClass().removeAll("clicked_button", "other_button");
            button.getStyleClass().add("other_button");
        }

        // Gán class "clicked_button" cho button vừa được nhấn
        clickedButton.getStyleClass().remove("other_button");
        clickedButton.getStyleClass().add("clicked_button");
    }
    @FXML
    private StackPane contentArea;

    // Sự kiện khi click vào "Quản lý Chi Phí"

    @FXML
    private void onManageFees() {
        handleButtonClick(feeButton);
        loadPage("/view/fee/fee-management.fxml");
    }



    @FXML
    private void onManageHome(){
        handleButtonClick(homeButton);
        loadPage("/view/homepage/home-management.fxml");
    }

    @FXML
    private void onManageResident(){
        handleButtonClick(residentButton);
        User currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Không tìm thấy người dùng hiện tại.");
            return;
        }

        String role = currentUser.getRole().name(); // giả sử bạn dùng enum hoặc String để lưu role

        if ("USER".equalsIgnoreCase(role)) {
            loadPage("/view/resident/resident_edit.fxml");
        } else {
            loadPage("/view/resident/resident_table.fxml");
        }
    }

    @FXML
    private void onManageBill(){
        handleButtonClick(billButton);
        ContextMenu contextMenu = new ContextMenu();

        MenuItem paymentItem = new MenuItem("Thanh toán");
        MenuItem paymentHistoryItem = new MenuItem("Lịch sử Thanh toán");
        MenuItem contributionHistoryItem = new MenuItem("Lịch sử Đóng góp");


        paymentHistoryItem.setOnAction(e -> loadPage("/view/payment/payment-view.fxml"));
        paymentItem.setOnAction(e -> loadPage("/view/bill/bill-management.fxml"));
        contributionHistoryItem.setOnAction(e -> loadPage("/view/contribution/contribution-management.fxml"));
        contextMenu.getItems().addAll(paymentItem, paymentHistoryItem, contributionHistoryItem);
        contextMenu.show(billButton, Side.RIGHT, 0, 0);
    }

    @FXML
    private void onManageReport(){
        handleButtonClick(reportButton);
        User currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Không tìm thấy người dùng hiện tại.");
            return;
        }

        String role = currentUser.getRole().name(); // giả sử bạn dùng enum hoặc String để lưu role

        if ("USER".equalsIgnoreCase(role)) {
            loadPage("/view/report/user_report.fxml");
        }
        else {
            loadPage("/view/report/report_list.fxml");
        }
        //notificationController.setComplaintMode(true);
    }

    @FXML
    private void onManageApartment(){
        handleButtonClick(supportButton);

        User currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Không tìm thấy người dùng hiện tại.");
            return;
        }

        String role = currentUser.getRole().name(); // giả sử bạn dùng enum hoặc String để lưu role

        if ("USER".equalsIgnoreCase(role)) {
            loadPage("/view/apartment/apartment_edit.fxml");
        } else {
            loadPage("/view/apartment/apartment_table.fxml");
        }

    }

    @FXML
    private void onManageNotification(){
        handleButtonClick(notificationButton);

        User currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Không tìm thấy người dùng hiện tại.");
            return;
        }

        String role = currentUser.getRole().name(); // giả sử bạn dùng enum hoặc String để lưu role

        if ("USER".equalsIgnoreCase(role)) {
            loadPage("/view/notification/user_notification.fxml");
        } else {
            loadPage("/view/notification/admin_notification.fxml");
        }
    }


    // Hiển thị thông báo
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void loadPage(String fxmlPath) {
        try {
            System.out.println("Đang tải: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean); // Spring sẽ tự inject controller và các dependency

            Pane newPage = loader.load();

            if (contentArea != null) {
                contentArea.getChildren().setAll(newPage);
            } else {
                System.err.println("contentArea chưa được khởi tạo.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi khi tải trang: " + fxmlPath + "\n" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi không xác định: " + e.getMessage());
        }
    }

    /** Account **/
    @FXML private Button accountButton;
    @FXML private MenuItem usernameItem;
    @FXML private ContextMenu accountMenu;

    @FXML
    private void handleOpenMenu() {
        if (accountMenu != null) {
            accountMenu.show(accountButton, Side.BOTTOM, 0, 10);
        }
    }

    @FXML
    private void handleLogOut() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/login&register/main.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        handleQuit();
        Stage primaryStage = new Stage();
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 850, 650); // Chỉnh lại kích thước cho đúng với FXML
        primaryStage.setTitle("Đăng nhập");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setResizable(false); // Ngăn người dùng chỉnh sửa kích thước
        primaryStage.show();
    }

    @FXML
    private void handleQuit(){
        Stage homeStage = (Stage) accountButton.getScene().getWindow();
        homeStage.close();
    }


}