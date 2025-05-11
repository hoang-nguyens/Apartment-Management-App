package repositories;

import models.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserIdAndIsReadFalse(Long userId);

    List<UserNotification> findByUserId(Long userId);

    List<UserNotification> findByUserIdAndIsReadTrue(Long userId);

    void deleteByNotificationId(Long notificationId);
}