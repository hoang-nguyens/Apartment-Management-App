package services;

import models.Invoice;
import models.Payment;
import models.User;
import models.enums.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.PaymentRepository;

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

    public Payment createPayment(User user, Invoice invoice, PaymentMethod paymentMethod,String note) {
        if (paymentMethod == null) paymentMethod = PaymentMethod.CASH;
        Payment payment = new Payment(invoice, user, invoice.getAmount(), paymentMethod, note);
        return createPayment(payment);
    }
}