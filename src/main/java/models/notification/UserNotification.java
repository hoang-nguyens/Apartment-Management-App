package models.notification;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import models.BaseModel;
import models.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications")
@Getter
@Setter
public class UserNotification extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;

    private Boolean isRead = false;

    private LocalDateTime readAt;
}