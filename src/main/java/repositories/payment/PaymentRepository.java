package repositories.payment;

import models.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByResidentId(Long residentId);

    @Query("""
        SELECT p FROM Payment p
        WHERE p.resident.id = :userId
        AND p.paymentDate BETWEEN :startDate AND :endDate
        """)
    List<Payment> findPaymentsByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}