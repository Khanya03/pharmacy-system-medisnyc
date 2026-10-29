package za.ac.cput.medisnyc.controller;

/* ReportController.java
   Module 6: Reports & Administration - Report API / Dashboard Statistics API.
   Author: Phemelo
*/

import za.ac.cput.medisnyc.service.ReportService;
import za.ac.cput.medisnyc.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService, PaymentService paymentService) {
        this.reportService = reportService;
        this.paymentService = paymentService;
    }

    @GetMapping("/dashboard-statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        return ResponseEntity.ok(reportService.getDashboardStatistics());
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getRevenueReport(from, to));
    }

    @GetMapping(value = "/payments/export", produces = "text/csv")
    public ResponseEntity<String> exportPayments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<Map<String, Object>> rows = paymentService.export(from, to);
        StringBuilder csv = new StringBuilder("payment id,receipt,date,order,patient,amount,brand,last4,status\n");
        for (Map<String, Object> row : rows) {
            csv.append(csv(row.get("paymentId"))).append(',').append(csv(row.get("receipt"))).append(',')
                    .append(csv(row.get("date"))).append(',').append(csv(row.get("order"))).append(',')
                    .append(csv(row.get("patient"))).append(',').append(csv(row.get("amount"))).append(',')
                    .append(csv(row.get("brand"))).append(',').append(csv(row.get("last4"))).append(',')
                    .append(csv(row.get("status"))).append('\n');
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments.csv")
                .body(csv.toString());
    }

    private final PaymentService paymentService;

    private String csv(Object value) {
        String text = value == null ? "" : value.toString();
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}