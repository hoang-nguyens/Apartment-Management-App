package controllers;

import models.Apartment;
import models.Invoice;
import models.enums.InvoiceStatus;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.ApartmentService;
import services.InvoiceService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final ApartmentService apartmentService;
    @Autowired
    public InvoiceController(InvoiceService invoiceService, ApartmentService apartmentService) {
        this.invoiceService = invoiceService;
        this.apartmentService = apartmentService;
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices(
            @RequestParam(required = false) Long userId
    ) {
        if (userId != null) {
            return ResponseEntity.ok(invoiceService.getInvoiceByUserId(userId));
        } else {
            return ResponseEntity.ok(invoiceService.getInvoices());
        }
    }

    @GetMapping("/apartments")
    public ResponseEntity<List<Invoice>> getInvoice(
            @RequestParam(required = false) String apartment,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) InvoiceStatus status
    ) {
        if (apartment != null) {
            Apartment apartmentRoom = apartmentService.getApartmentByRoomNumber(apartment);
            if (apartmentRoom != null) {
                return ResponseEntity.ok(invoiceService.getFilterInvoice(apartmentRoom.getId(), category, status));
            } else {
                return ResponseEntity.ok(new ArrayList<Invoice>());
            }
        } else {
            return ResponseEntity.ok(invoiceService.getFilterInvoice(null, category, status));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam(defaultValue = "PAID") InvoiceStatus status
    ) {
        try {
            return ResponseEntity.ok(invoiceService.updateStatus(id ,status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}