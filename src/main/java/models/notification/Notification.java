package models.notification;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import models.BaseModel;
import models.user.User;
import models.enums.NotificationType;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification extends BaseModel {

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
}