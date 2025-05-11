package services.login_signup;

import repositories.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ForgotPasswordService {
    private final UserRepository userRepository;

    public ForgotPasswordService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

//    public Optional<User> getUserByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }
}

