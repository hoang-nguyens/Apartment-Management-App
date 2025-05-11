package repositories;

import models.Notification;
import models.UserNotification;
import models.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCreatedByRoleIn(List<Role> roles);
}