package repositories.report;

import models.report.Report;
import models.report.ReportResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportResponseRepository extends JpaRepository<ReportResponse, Long> {
    Optional<ReportResponse> findByReport(Report report);

    List<ReportResponse> findAllByReport(Report report);
}