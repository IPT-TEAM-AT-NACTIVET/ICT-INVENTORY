package tz.go.nactvet.ict_inventory_management.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tz.go.nactvet.ict_inventory_management.dto.ReportFilterOptionsResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportResponse;
import tz.go.nactvet.ict_inventory_management.service.ReportService;

@RestController
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/data")
    public ResponseEntity<ReportResponse> getReportData(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ownershipType,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) String office,
            @RequestParam(required = false) String userOfAsset,
            @RequestParam(required = false) String registeredBy,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam(defaultValue = "overview") String groupBy) {
        return ResponseEntity.ok(reportService.getReportData(
                search, deviceTypeId, status, ownershipType, zoneId,
                office, userOfAsset, registeredBy, from, to, groupBy));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<ReportFilterOptionsResponse> getFilterOptions() {
        return ResponseEntity.ok(reportService.getFilterOptions());
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ownershipType,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) String office,
            @RequestParam(required = false) String userOfAsset,
            @RequestParam(required = false) String registeredBy,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to) {

        String csv = reportService.exportCsv(search, deviceTypeId, status, ownershipType,
                zoneId, office, userOfAsset, registeredBy, from, to);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ict-inventory-report.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
