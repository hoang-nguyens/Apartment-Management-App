package repositories.report;

import models.report.Report;
import models.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatus(ReportStatus status);

    List<Report> findByUserIdOrderByCreatedAtDesc(Long userId);
}