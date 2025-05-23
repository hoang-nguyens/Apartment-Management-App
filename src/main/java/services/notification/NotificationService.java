package services.notification;

import jakarta.transaction.Transactional;
import models.notification.Notification;
import models.user.User;
import models.notification.UserNotification;
import models.enums.NotificationType;
import models.enums.Role;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import repositories.notification.NotificationRepository;
import repositories.notification.UserNotificationRepository;
import repositories.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserNotificationRepository userNotificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.userRepository = userRepository;
    }

    // Gửi thông báo tới nhiều user
    public void sendNotificationToUsers(String title, String message, NotificationType type, Long adminId, List<Long> userIds) {
        User admin = userRepository.findById(adminId).orElseThrow();
        Notification noti = new Notification();
        noti.setTitle(title);
        noti.setMessage(message);
        noti.setType(type);
        noti.setCreatedBy(admin);

        noti = notificationRepository.save(noti);

        List<UserNotification> userNotis = new ArrayList<>();
        for (Long uid : userIds) {
            User u = userRepository.findById(uid).orElseThrow();
            UserNotification un = new UserNotification();
            un.setUser(u);
            un.setNotification(noti);
            userNotis.add(un);
        }

        userNotificationRepository.saveAll(userNotis);
    }

    // Đánh dấu đã đọc
    public void markAsRead(Long userNotificationId) {
        UserNotification un = userNotificationRepository.findById(userNotificationId).orElseThrow();
        un.setIsRead(true);
        un.setReadAt(LocalDateTime.now());
        userNotificationRepository.save(un);
    }

    // Lấy thông báo chưa đọc
    public List<UserNotification> getUnreadNotifications(Long userId) {
        return userNotificationRepository.findByUserIdAndIsReadFalse(userId);
    }
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    // Lấy toàn bộ thông báo của user (cả đã đọc lẫn chưa đọc)
    public List<UserNotification> getAllUserNotifications(Long userId) {
        return userNotificationRepository.findByUserId(userId);
    }
    public List<UserNotification> getReadNotifications(Long userId) {
        return userNotificationRepository.findByUserIdAndIsReadTrue(userId);
    }
    @Transactional
    public void deleteNotification(Long notificationId) {
        // Xóa tất cả bản ghi UserNotification liên quan
        userNotificationRepository.deleteByNotificationId(notificationId);

        // Sau đó xóa Notification
        notificationRepository.deleteById(notificationId);
    }

    public List<Notification> getNotificationsCreatedByUsers() {
        return notificationRepository.findByCreatedByRoleIn(List.of(Role.USER));
    }

    // Lấy thông báo được tạo bởi người có role là ADMIN
    public List<Notification> getNotificationsCreatedByAdmins() {
        List<Role> adminRoles = List.of(Role.ADMIN, Role.ADMIN_ROOT);
        return notificationRepository.findByCreatedByRoleIn(adminRoles);
    }


}