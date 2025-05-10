package controllers;

import models.Invoice;
import models.Payment;
import models.User;
import models.enums.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.InvoiceService;
import services.PaymentService;
import services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;
    private final InvoiceService invoiceService;

    @Autowired
    public PaymentController(PaymentService paymentService, UserService userService, InvoiceService invoiceService) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.invoiceService = invoiceService;
    }

    @PostMapping("/manual")
    public ResponseEntity<Payment> manualPayment(
            @RequestParam Long invoiceId,
            @RequestParam Long payerId,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false, defaultValue = "Thu cong") String note
            ) {
        try {
            User resident = userService.getUserById(payerId);
            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            System.out.println(invoice.getId() + " " + resident.getId());
            return ResponseEntity.ok(paymentService.createPayment(resident, invoice, paymentMethod, note));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("user/{id}")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentsByUserId(id));
    }
}
