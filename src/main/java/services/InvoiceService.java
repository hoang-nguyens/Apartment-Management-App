package services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import models.*;
import models.enums.FeeType;
import models.enums.InvoiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.InvoiceRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final UserService userService;
    private final FeeService feeService;
    private final ApartmentService apartmentService;
    private final FeeCategoryService feeCategoryService;

    LocalDate today = LocalDate.now();
    YearMonth yearMonth = YearMonth.from(today);
    LocalDate monthlyIssueDate = yearMonth.atDay(1);
    LocalDate monthyDueDate = yearMonth.atDay(10);

    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository, UserService userService, FeeService feeService, ApartmentService apartmentService, FeeCategoryService feeCategoryService) {
        this.invoiceRepository = invoiceRepository;
        this.userService = userService;
        this.feeService = feeService;
        this.apartmentService = apartmentService;
        this.feeCategoryService = feeCategoryService;
    }

    public void createInvoice(Invoice invoice) {
        invoiceRepository.save(invoice);
    }

    public void createMonthlyInvoices() {
        List<FeeCategory> feeCategories = feeCategoryService.getFeeCategoriesByType(FeeType.MANDATORY);
        List<String> categories = feeCategories.stream().map(FeeCategory::getName).toList();

        List<User> users = userService.getAllUsersWithUserRole();
        for (User user : users) {
            List<Apartment> apartmentList = apartmentService.getAllApartmentsByOwner(user);
            if (apartmentList.isEmpty()) {
                continue;
            }
            for (Apartment apartment : apartmentList) {
                for (String category : categories) {
                    createInvoice(user, category, apartment);
                }
            }
        }
    }

    public void createMonthlyInvoices(User user) {
        List<FeeCategory> feeCategories = feeCategoryService.getFeeCategoriesByType(FeeType.MANDATORY);
        List<String> categories = feeCategories.stream().map(FeeCategory::getName).toList();
        List<Apartment> apartmentList = apartmentService.getAllApartmentsByOwner(user);

        for (Apartment apartment : apartmentList) {
            for (String category : categories) {
                createInvoice(user, category, apartment);
            }
        }
    }

    public void createInvoice(User user, String category, Apartment apartment) {
        if (checkExistedInvoice(apartment, category)) {
            return;
        }
        BigDecimal totalFee = BigDecimal.ZERO;
        List<Fee> feeList = feeService.getAllActiveFeesByCategoryAndSubCategory(category, null);
        for (Fee fee : feeList) {
            BigDecimal amount = calculateAmount(user, fee, apartment);

            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                System.out.println(amount.toPlainString());
                totalFee = totalFee.add(amount);
                // goi invoice service them vao db
            }
        }
        if (totalFee.compareTo(BigDecimal.ZERO) > 0) {
            Invoice invoice = new Invoice(user, monthlyIssueDate, monthyDueDate, category, totalFee, apartment);
            createInvoice(invoice);
        }
    }

    private boolean checkExistedInvoice(Apartment apartment, String category){
        return invoiceRepository.existsByApartmentAndCategoryAndIssueDate(apartment, category, monthlyIssueDate);
    }

    private BigDecimal calculateAmount(User user, Fee fee, Apartment apartment) {
//        if (checkExistedInvoice(user, fee)) {
//            return BigDecimal.ZERO;
//        }
//        List<Apartment> apartmentList = apartmentService.getAllApartmentsByOwner(user);
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal amount;
//        if (apartmentList.isEmpty()) {
//            return BigDecimal.ZERO;
//        }

        switch (fee.getUnit()) {
            case M2:
                float area = apartment.getArea();
                amount = BigDecimal.valueOf(area).multiply(fee.getAmount());
                totalAmount = totalAmount.add(amount).setScale(0, RoundingMode.HALF_UP);;
                break;
            case VEHICLE:
                int vehicleCount = 0;
                if (fee.getSubCategory().toLowerCase().contains("xe máy")){
                    vehicleCount = apartment.getMotorbikeCount();
                } else if (fee.getSubCategory().toLowerCase().contains("ô tô")){
                    vehicleCount = apartment.getCarCount();
                }
                amount = BigDecimal.valueOf(vehicleCount).multiply(fee.getAmount());
                totalAmount = totalAmount.add(amount).setScale(0, RoundingMode.HALF_UP);
                break;
            default:
                return BigDecimal.ZERO;
        }
        return totalAmount;
    }

    public Invoice updateStatus(Long id, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found"));
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalArgumentException("Invoice already paid");
        }
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    public void updateOverdueInvoice() {
        List<Invoice> overdueInvoices = invoiceRepository.findByDueDateBeforeAndStatusNot(today, InvoiceStatus.OVERDUE);
        for (Invoice invoice : overdueInvoices) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
        }
    }


    public List<Invoice> getInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getInvoiceByUser(User user) {
        return invoiceRepository.findByUser(user);
    }

    public List<Invoice> getInvoiceByUserId(long userId) {
        return invoiceRepository.findByUserId(userId);
    }
}