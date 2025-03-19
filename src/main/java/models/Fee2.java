package models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import models.enums.BillPeriod;
import models.enums.FeeUnit;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "fees2")
public class Fee2 extends BaseModel{
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private FeeCategory feeCategory;    // đây là id cuar sub_category

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FeeUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BillPeriod billPeriod;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = true)
    private LocalDate endDate;

    public Fee2(){};

    public Fee2(FeeCategory feeCategory, BigDecimal amount, FeeUnit unit, BillPeriod billPeriod, LocalDate startDate, LocalDate endDate) {
        this.feeCategory = feeCategory;
        this.amount = amount;
        this.unit = unit;
        this.billPeriod = billPeriod;
        this.startDate = startDate;
        this.endDate = endDate;
    }}
