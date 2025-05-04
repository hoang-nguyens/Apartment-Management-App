package repositories;

import models.Apartment;
import models.Fee;
import models.Invoice;
import models.User;
import models.enums.InvoiceStatus;
import models.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>{
    List<Invoice> findAll();
    List<Invoice> findByUser(User user);
    List<Invoice> findByUserId(Long id);

    @Query("""
    SELECT i FROM Invoice i
    WHERE (:apartmentId IS NULL OR i.apartment.id = :apartmentId)
    AND (:category IS NULL OR i.category = :category)
    AND (:status IS NULL OR i.status = :status)
    """)
    List<Invoice> findFilteredInvoices(
            @Param("apartmentId") Long apartmentId,
            @Param("category") String category,
            @Param("status") InvoiceStatus status
    );

    boolean existsByApartmentAndCategoryAndIssueDate(Apartment apartment, String category, LocalDate issueDate);
    List<Invoice> findByDueDateBeforeAndStatusNot(LocalDate dueDate, InvoiceStatus status);
}