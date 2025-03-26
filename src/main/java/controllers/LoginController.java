package controllers;

import app.MainApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
//import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import models.User;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.net.URL;
import java.util.ResourceBundle;

import services.UserService;

@Controller
public class LoginController {
    private final UserService userService;
    private boolean oneTime=true;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label loginStatusLabel;

    @FXML private VBox loginBox;
    @FXML private VBox registerBox;
    @FXML private Hyperlink registerLink;
    @FXML private Hyperlink loginLink;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
//        if (oneTime){
//            this.userService.registerUser("canhtes1", "canhtes1@gmail.com", "canh2k5");
//            oneTime=false;
//            System.out.println("create canh test");
//        }

        if (username.isBlank() || password.isBlank()) {
            loginStatusLabel.setText("Vui lòng nhập thông tin");
            return;
        }

        Optional<String> hashedPasswordOpt = userService.getPasswordByUsername(username);
        if (hashedPasswordOpt.isEmpty()) {
            loginStatusLabel.setText("Tên đăng nhập sai!");
        } else if (!BCrypt.checkpw(password, hashedPasswordOpt.get())) {
            loginStatusLabel.setText("Mật khẩu sai!");
        } else {
            loginStatusLabel.setText("Đăng nhập thành công!");
            Optional<User> user = userService.loginUser(username, password);
            if (user.isPresent()) {
                Authentication auth = new UsernamePasswordAuthenticationToken(user.get(), null, null);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }


            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/fee-management.fxml"));
                fxmlLoader.setControllerFactory(MainApplication.springContext::getBean);
                Scene feeScene = new Scene(fxmlLoader.load(), 400, 300);
                stage.setScene(feeScene);
                stage.setTitle("Quản lý khoản thu");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showLoginBox() {
        loginBox.setVisible(true);
        loginBox.setManaged(true);
        registerBox.setVisible(false);
        registerBox.setManaged(false);
    }

    private void showRegisterBox() {
        registerBox.setVisible(true);
        registerBox.setManaged(true);
        loginBox.setVisible(false);
        loginBox.setManaged(false);
    }

    @FXML
    private void handleRegisterClick() {
        showRegisterBox();
        System.out.println("Hyperlink clicked - Chuyển sang đăng ký");
    }

    @FXML
    private void handleLoginClick(){
        showLoginBox();
    }

}