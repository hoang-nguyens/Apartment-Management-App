package models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import models.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment extends BaseModel{

    @ManyToOne
    @JoinColumn(name = "invoice_id", referencedColumnName = "id", nullable = false)
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "resident_id", referencedColumnName = "id", nullable = false)
    private User resident;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate = new Date();

    @Enumerated(EnumType.STRING)
    // null tức là giao dịch chưa được ghi nhận
    private PaymentMethod paymentMethod;

    @Column(unique = true)
    private String transactionId;

    private String note;

    public Payment() {}

    public Payment(Invoice invoice, User resident, BigDecimal amount, PaymentMethod paymentMethod, String note) {
        this.invoice = invoice;
        this.resident = resident;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.note = note;
    }

    public Payment(Invoice invoice, User resident, BigDecimal amount, String transactionId, PaymentMethod paymentMethod, String note) {
        new Payment(invoice, resident, amount, paymentMethod, note);
        this.transactionId = transactionId;
    }
}