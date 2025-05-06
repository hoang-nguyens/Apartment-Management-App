package controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;
import services.EmailService;
import services.ForgotPasswordService;
import services.UserService;
import org.springframework.web.bind.annotation.*;


@Controller
public class UserUIController {
    protected final UserService userService;
    protected final EmailService emailService;
    protected final ForgotPasswordService forgotPasswordService;
    @FXML
    private VBox loginBox;
    @FXML private VBox registerBox;
    @FXML private VBox confirmBox;
    @FXML private VBox forgotPasswordBox;
    public UserUIController(UserService userService, EmailService emailService, ForgotPasswordService forgotPasswordService) {
        this.userService = userService;
        this.emailService = emailService;
        this.forgotPasswordService = forgotPasswordService;
    }
    protected void showLoginBox() {
        loginBox.setVisible(true);
        loginBox.setManaged(true);
        registerBox.setVisible(false);
        registerBox.setManaged(false);
        confirmBox.setVisible(false);
        confirmBox.setManaged(false);
        forgotPasswordBox.setVisible(false);
        forgotPasswordBox.setManaged(false);
    }

    protected void showRegisterBox() {
        registerBox.setVisible(true);
        registerBox.setManaged(true);
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        confirmBox.setVisible(false);
        confirmBox.setManaged(false);
        forgotPasswordBox.setVisible(false);
        forgotPasswordBox.setManaged(false);
    }

    protected void showConfirmBox(){
        confirmBox.setVisible(true);
        confirmBox.setManaged(true);
        registerBox.setVisible(false);
        registerBox.setManaged(false);
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        forgotPasswordBox.setVisible(false);
        forgotPasswordBox.setManaged(false);
    }

    protected void showForgotPasswordBox(){
        forgotPasswordBox.setVisible(true);
        forgotPasswordBox.setManaged(true);
        registerBox.setVisible(false);
        registerBox.setManaged(false);
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        confirmBox.setVisible(false);
        confirmBox.setManaged(false);
    }
}