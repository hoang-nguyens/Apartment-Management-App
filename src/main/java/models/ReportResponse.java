package models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne  // Sửa từ @OneToOne sang @ManyToOne vì 1 báo cáo có thể có nhiều phản hồi
    private Report report;

    @ManyToOne
    private User admin;

    @Column(columnDefinition = "TEXT")
    private String responseText;

    private LocalDateTime respondedAt = LocalDateTime.now();

    // Thêm phương thức getter alias nếu muốn dùng getCreatedAt() như trong code view
    public LocalDateTime getCreatedAt() {
        return respondedAt;
    }
}
