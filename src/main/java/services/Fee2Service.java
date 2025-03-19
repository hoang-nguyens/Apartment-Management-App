package services;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import models.Fee;
import models.Fee2;
import models.FeeCategory;
import models.enums.BillPeriod;
import models.enums.FeeUnit;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class Fee2Service {
    @Autowired
    private FeeRepository feeRepository;

    @Autowired
    private FeeCategoryRepository feeCategoryRepository;
    @Autowired
    private Fee2Repository fee2Repository;

    public Fee2 createFee(FeeCategory feeCategory,
                          FeeCategory feeSubCategory,
                          BigDecimal amount,
                          FeeUnit feeUnit,
                          BillPeriod billPeriod,
                          LocalDate start_date,
                          @Nullable LocalDate end_date) {
        FeeCategory category = feeCategoryRepository.findByNameAndParentIsNull(feeCategory.getName());
        if (category == null) {
            category = new FeeCategory(feeCategory.getName(), null);
            feeCategoryRepository.save(category);
        }
        FeeCategory subCategory = feeCategoryRepository.findByNameAndParent(feeSubCategory.getName(), category);
        if (subCategory == null) {
            subCategory = new FeeCategory(feeSubCategory.getName(), category);
            feeCategoryRepository.save(subCategory);
        }

        Fee2 fee = new Fee2(subCategory, amount, feeUnit, billPeriod, start_date, end_date);
        return fee2Repository.save(fee);
    }

    public Fee2 updateFee(Long feeId,
                          BigDecimal amount,
                          FeeUnit unit,
                          BillPeriod billPeriod,
                          LocalDate start_date,
                          LocalDate end_date){
        Fee2 fee = fee2Repository.findById(feeId).orElse(null);
        if (fee==null) {
            throw new EntityNotFoundException("Fee not found with id " + feeId);
        }
        fee.setAmount(amount);
        fee.setUnit(unit);
        fee.setBillPeriod(billPeriod);
        fee.setStartDate(start_date);
        fee.setEndDate(end_date);
        return fee2Repository.save(fee);
    }

    public void deleteFee(Long feeId) {
        if (!fee2Repository.existsById(feeId)) {
            throw new EntityNotFoundException("Fee not found with ID: " + feeId);
        }
        fee2Repository.deleteById(feeId);
    }

    public Fee2 getFeeById(Long feeId) {
        return fee2Repository.findById(feeId)
                .orElseThrow(() -> new EntityNotFoundException("Fee not found with ID: " + feeId));
    }

    public List<Fee2> getFeeByCategoryId(Long categoryId) {
        return fee2Repository.findByFeeCategoryId(categoryId);
    }

    public List<Fee2> getAllFees() {
        return fee2Repository.findAll();
    }
}
