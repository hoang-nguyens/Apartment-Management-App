package repositories.invoice;

import models.apartment.Apartment;
import models.invoice.Invoice;
import models.user.User;
import models.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>{
    List<Invoice> findAll();
    List<Invoice> findAllByUser(User user);
    List<Invoice> findAllByUserId(Long id);

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
    List<Invoice> findAllByDueDateBeforeAndStatusNot(LocalDate dueDate, InvoiceStatus status);

    List<Invoice> findAllByStatusNot(InvoiceStatus status);
    List<Invoice> findAllByUserIdAndStatusNot(Long userId, InvoiceStatus status);
}