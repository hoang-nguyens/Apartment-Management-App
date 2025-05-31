package repositories.user;

import models.enums.Role;
import models.resident.Resident;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import models.user.User;

@Component
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    @Cacheable(value = "userById", key = "#id")
    Optional<User> findById(Long id);

    List<User> findByRole(Role role);

}