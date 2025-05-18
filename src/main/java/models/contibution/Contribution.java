package models.contibution;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import models.BaseModel;
import models.fee.Fee;
import models.user.User;
import models.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name="contributions")
public class Contribution extends BaseModel {
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "fee_id", referencedColumnName = "id", nullable = false)
    private Fee fee;

    @DecimalMin(value = "0", message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate = new Date();

    @Enumerated(EnumType.STRING)
    // null tức là giao dịch chưa được ghi nhận
    private PaymentMethod paymentMethod;

    @Column(unique = true)
    private String transactionId;

    private String note;

    public Contribution() {};

    public Contribution(User user, Fee fee, BigDecimal amount, PaymentMethod paymentMethod, String note) {
        this.fee = fee;
        this.user = user;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.note = note;
    }
}