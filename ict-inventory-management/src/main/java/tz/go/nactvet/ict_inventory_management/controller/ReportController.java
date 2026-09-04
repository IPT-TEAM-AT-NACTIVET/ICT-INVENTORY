package tz.go.nactvet.ict_inventory_management.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.PagedResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportSummaryResponse;
import tz.go.nactvet.ict_inventory_management.service.ReportService;

@RestController
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryResponse> getSummary(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(reportService.getSummary(search));
    }

    @GetMapping("/inventory")
    public ResponseEntity<PagedResponse<AssetResponse>> getInventoryReport(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reportService.getInventoryReport(search, page, size));
    }

    @GetMapping("/by-zone")
    public ResponseEntity<ReportResponse> getReportByZone(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(reportService.getReportByZone(search));
    }

    @GetMapping("/by-office")
    public ResponseEntity<ReportResponse> getReportByOffice(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(reportService.getReportByOffice(search));
    }

    @GetMapping("/by-device-type")
    public ResponseEntity<ReportResponse> getReportByDeviceType(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(reportService.getReportByDeviceType(search));
    }

    @GetMapping("/by-status")
    public ResponseEntity<ReportResponse> getReportByStatus(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(reportService.getReportByStatus(search));
    }

    @GetMapping("/by-ownership")
    public ResponseEntity<ReportResponse> getReportByOwnership(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(reportService.getReportByOwnership(search));
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<AssetResponse>> getFilteredAssets(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(reportService.getFilteredAssets(search));
    }

    @GetMapping("/inventory/export/csv")
    public ResponseEntity<byte[]> exportInventoryCsv(
            @RequestParam(required = false) String search) {

        String csv = reportService.exportInventoryCsv(search);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}
