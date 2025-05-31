package schedulers;

import models.enums.BillPeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import services.invoice.InvoiceService;

@Component
public class InvoiceScheduler {
    private final InvoiceService invoiceService;
    @Autowired
    public InvoiceScheduler(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Ho_Chi_Minh")
    public void createMonthlyInvoice() {
        System.out.println("Creating Monthly invoice");
        invoiceService.createInvoices(BillPeriod.MONTHLY);
    }

    @Scheduled(cron = "0 5 0 1 1,4,7,10 *", zone = "Asia/Ho_Chi_Minh")
    public void createQuarterlyInvoice() {
        invoiceService.createInvoices(BillPeriod.QUARTERLY);
    }

    @Scheduled(cron = "0 10 0 1 1 *", zone = "Asia/Ho_Chi_Minh")
    public void createYearlyInvoice() {
        invoiceService.createInvoices(BillPeriod.YEARLY);
    }

    @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void createOnetimeInvoice() {
        invoiceService.createInvoices(BillPeriod.ONE_TIME);
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Ho_Chi_Minh")
    public void updateOverdueInvoice() {
        invoiceService.updateOverdueInvoice();
    }
}
