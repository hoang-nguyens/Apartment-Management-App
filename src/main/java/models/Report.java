package models;

import models.enums.ReportStatus;
import jakarta.persistence.*;
import javafx.beans.property.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Transient
    private final LongProperty idProperty = new SimpleLongProperty();

    @Transient
    private final StringProperty titleProperty = new SimpleStringProperty();

    @Transient
    private final ObjectProperty<ReportStatus> statusProperty = new SimpleObjectProperty<>();

    @Transient
    private final StringProperty createdAtProperty = new SimpleStringProperty();

    @Transient
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime respondedAt;

    // Cập nhật các property khi có thay đổi
    @PostLoad
    private void updateProperties() {
        idProperty.set(id);
        titleProperty.set(title);
        statusProperty.set(status);
        createdAtProperty.set(createdAt.format(formatter));
    }

    // Các phương thức getter cho property
    public LongProperty idProperty() {
        return idProperty;
    }

    public StringProperty titleProperty() {
        return titleProperty;
    }

    public ObjectProperty<ReportStatus> statusProperty() {
        return statusProperty;
    }

    public StringProperty createdAtProperty() {
        return createdAtProperty;
    }

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ReportResponse> responses = new ArrayList<>();

    public List<ReportResponse> getResponses() {
        return responses;
    }

}