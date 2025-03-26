package services;

import models.Fee;
import models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.InvoiceRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final UserService userService;
    private final FeeService feeService;

    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository, UserService userService, FeeService feeService) {
        this.invoiceRepository = invoiceRepository;
        this.userService = userService;
        this.feeService = feeService;
    }

    public void createMonthlyInvoices(){
        LocalDate today = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(today);
        LocalDate dueDate = yearMonth.atDay(10);

        List<Fee> monthlyFees = feeService.getAllActiveForcedFees();
        for (Fee f : monthlyFees) {
            List<User> users = userService.getAllUsersWithUserRole();
            for (User user : users) {
                
            }
        }
    }
}
