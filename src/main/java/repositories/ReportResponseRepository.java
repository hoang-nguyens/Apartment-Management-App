package repositories;

import models.Report;
import models.ReportResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportResponseRepository extends JpaRepository<ReportResponse, Long> {
    Optional<ReportResponse> findByReport(Report report);

    List<ReportResponse> findAllByReport(Report report);
}