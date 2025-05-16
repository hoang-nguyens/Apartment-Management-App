package services.payment;

import models.invoice.Invoice;
import models.payment.Payment;
import models.resident.Resident;
import models.user.User;
import models.enums.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.payment.PaymentRepository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(Payment payment) {
        System.out.println(payment.toString());
        return paymentRepository.save(payment);
    }

    public Payment createPayment(User user, Invoice invoice, PaymentMethod paymentMethod, String note) {
        if (paymentMethod == null) paymentMethod = PaymentMethod.CASH;
        Payment payment = new Payment(invoice, user, invoice.getAmount(), paymentMethod, note);
        return createPayment(payment);
    }

    public BigDecimal calculateMonthlyPayment(Long userId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        Date sqlStartDate = Date.valueOf(startDate);
        Date sqlEndDate = Date.valueOf(endDate);

        List<Payment> monthlyPayments = paymentRepository.findPaymentsByUserIdAndDateRange(userId, sqlStartDate, sqlEndDate);
        System.out.println(monthlyPayments.toString());
        return monthlyPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findAllByResidentId(userId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}