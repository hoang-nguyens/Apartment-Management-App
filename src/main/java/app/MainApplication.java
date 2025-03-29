package app;

import config.AppConfig;
import config.SecurityConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

@SpringBootApplication
@Import({AppConfig.class, SecurityConfig.class})
public class MainApplication extends Application {

    public static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Khởi động Spring Boot trước khi JavaFX chạy
        springContext = SpringApplication.run(MainApplication.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//        Arrays.stream(springContext.getBeanDefinitionNames()).sorted().forEach(System.out::println);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/fee-management.fxml"));
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/HomePage.fxml"));
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/login&register/main.fxml"));


        fxmlLoader.setControllerFactory(springContext::getBean);
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);

        primaryStage.setTitle("Quản lý khoản thu");
//        primaryStage.setTitle("Đăng nhập ");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Đóng Spring Boot khi ứng dụng JavaFX tắt
        springContext.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
