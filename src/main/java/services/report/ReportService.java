package services.report;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import models.report.Report;
import models.report.ReportResponse;
import models.user.User;
import models.enums.ReportStatus;
import org.springframework.stereotype.Service;
import repositories.report.ReportRepository;
import repositories.report.ReportResponseRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepo;
    private final ReportResponseRepository responseRepo;

    public Report submitReport(User user, String title, String description) {
        Report report = new Report();
        report.setUser(user);
        report.setTitle(title);
        report.setDescription(description);
        return reportRepo.save(report);
    }

    public List<Report> getPendingReports() {
        return reportRepo.findByStatus(ReportStatus.PENDING);
    }

    public ReportResponse respondToReport(Long reportId, User admin, String responseText) {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setStatus(ReportStatus.RESPONDED);
        report.setRespondedAt(LocalDateTime.now());
        reportRepo.save(report);

        ReportResponse response = new ReportResponse();
        response.setReport(report);
        response.setAdmin(admin);
        response.setResponseText(responseText);
        return responseRepo.save(response);
    }

    @Transactional
    public void deleteReport(Long reportId) {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        // Xóa tất cả các phản hồi liên quan đến report
        List<ReportResponse> responses = responseRepo.findAllByReport(report);
        if (!responses.isEmpty()) {
            responseRepo.deleteAll(responses);
        }

        // Xóa báo cáo
        reportRepo.delete(report);
    }


    public List<Report> getAllReports() {
        return reportRepo.findAll();
    }
    // Trong ReportService
    public List<Report> getAllReportsByUser(Long userId) {
        return reportRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
}