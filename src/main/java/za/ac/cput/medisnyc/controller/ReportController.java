package za.ac.cput.medisnyc.controller;

/* ReportController.java
   Module 6: Reports & Administration - Report API / Dashboard Statistics API.
   Author: Phemelo
*/

import za.ac.cput.medisnyc.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard-statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        return ResponseEntity.ok(reportService.getDashboardStatistics());
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport() {
        return ResponseEntity.ok(reportService.getRevenueReport());
    }
}