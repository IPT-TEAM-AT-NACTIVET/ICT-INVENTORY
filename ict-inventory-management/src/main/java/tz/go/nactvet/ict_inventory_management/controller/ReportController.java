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
import tz.go.nactvet.ict_inventory_management.dto.ReportResponse;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.OwnershipType;
import tz.go.nactvet.ict_inventory_management.enums.VerificationStatus;
import tz.go.nactvet.ict_inventory_management.service.ReportService;

@RestController
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/inventory")
    public ResponseEntity<ReportResponse> getInventoryReport(
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {
        return ResponseEntity.ok(reportService.getInventoryReport(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus));
    }

    @GetMapping("/by-directorate")
    public ResponseEntity<ReportResponse> getReportByDirectorate(
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {
        return ResponseEntity.ok(reportService.getReportByDirectorate(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus));
    }

    @GetMapping("/by-section")
    public ResponseEntity<ReportResponse> getReportBySection(
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {
        return ResponseEntity.ok(reportService.getReportBySection(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus));
    }

    @GetMapping("/by-zone")
    public ResponseEntity<ReportResponse> getReportByZone(
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {
        return ResponseEntity.ok(reportService.getReportByZone(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus));
    }

    @GetMapping("/by-unit")
    public ResponseEntity<ReportResponse> getReportByUnit(
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {
        return ResponseEntity.ok(reportService.getReportByUnit(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus));
    }

    @GetMapping("/by-office")
    public ResponseEntity<ReportResponse> getReportByOffice(
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {
        return ResponseEntity.ok(reportService.getReportByOffice(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus));
    }

    @GetMapping("/by-device-type")
    public ResponseEntity<ReportResponse> getReportByDeviceType(
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {
        return ResponseEntity.ok(reportService.getReportByDeviceType(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus));
    }

    @GetMapping("/by-status")
    public ResponseEntity<ReportResponse> getReportByStatus(
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {
        return ResponseEntity.ok(reportService.getReportByStatus(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus));
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<AssetResponse>> getFilteredAssets(
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {
        return ResponseEntity.ok(reportService.getFilteredAssets(
                assetNumber, serialNumber, deviceName, deviceTypeId, userId,
                directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus));
    }

    @GetMapping("/inventory/export/csv")
    public ResponseEntity<byte[]> exportInventoryCsv(
            @RequestParam(required = false) String assetNumber,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long directorateId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long officeId,
            @RequestParam(required = false) OwnershipType ownershipType,
            @RequestParam(required = false) DeviceStatus deviceStatus,
            @RequestParam(required = false) VerificationStatus verificationStatus) {

        String csv = reportService.exportInventoryCsv(
                assetNumber, serialNumber, deviceName, deviceTypeId, employeeId, userName,
                userId, directorateId, sectionId, unitId, zoneId, officeId,
                ownershipType, deviceStatus, verificationStatus);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}