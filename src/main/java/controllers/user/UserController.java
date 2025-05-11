package controllers.user;

import models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.login_signup.EmailService;
import services.login_signup.ForgotPasswordService;
import services.resident.ResidentService;
import services.user.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final ForgotPasswordService forgotPasswordService;
    private final ResidentService residentService;

    @Autowired
    public UserController(UserService userService,
                          EmailService emailService,
                          ForgotPasswordService forgotPasswordService,
                          ResidentService residentService) {
        this.userService = userService;
        this.emailService = emailService;
        this.forgotPasswordService = forgotPasswordService;
        this.residentService = residentService;
    }

    // Lấy danh sách tất cả user có role là USER
    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.getAllUsersWithUserRole();
        return ResponseEntity.ok(users);
    }

    // Tạo mới user - yêu cầu gửi JSON dạng User
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.registerUser(
                    user.getUsername(), user.getEmail(), user.getPasswordHash()
            );
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa user theo username
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        try {
            userService.deleteUserByUsername(username);
            return ResponseEntity.ok("Đã xóa thành công User với username: " + username);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy thông tin user theo username
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> userOpt = userService.findUserByUsername(username);
        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
}