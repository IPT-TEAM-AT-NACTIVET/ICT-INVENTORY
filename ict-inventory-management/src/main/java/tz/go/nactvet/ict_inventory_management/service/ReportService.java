package tz.go.nactvet.ict_inventory_management.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import tz.go.nactvet.ict_inventory_management.dto.AssetResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportFilterOptionsResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportSummaryResponse;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    public ReportService(AssetRepository assetRepository, AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.assetMapper = assetMapper;
    }

    /**
     * Single entry point for the Reports module. Fetches the full filtered asset
     * list once, then derives the KPI summary, grouped report items and the
     * detailed asset list from that exact same dataset so they can never diverge.
     */
    public ReportResponse getReportData(String search, Long deviceTypeId, String status,
            Long zoneId, String office, String userOfAsset,
            String registeredBy, LocalDate from, LocalDate to, String groupBy) {

        List<AssetResponse> assets = getFilteredAssets(search, deviceTypeId, status,
                zoneId, office, userOfAsset, registeredBy, from, to);

        ReportSummaryResponse summary = buildSummary(assets);

        List<ReportResponse.ReportItem> items;
        if (isGrouped(groupBy)) {
            items = buildGroupedItems(assets, groupBy);
        } else {
            items = List.of();
        }

        ReportResponse response = new ReportResponse();
        response.setReportType(groupBy == null ? "overview" : groupBy);
        response.setTotalAssets(assets.size());
        response.setSummary(summary);
        response.setAssets(assets);
        response.setItems(items);
        return response;
    }

    public ReportFilterOptionsResponse getFilterOptions() {
        List<String> offices = assetRepository.findDistinctOffices();

        ReportFilterOptionsResponse response = new ReportFilterOptionsResponse();
        response.setOffices(offices);
        response.setUsersOfAsset(assetRepository.findDistinctUsersOfAsset());

        List<ReportFilterOptionsResponse.RegistrarOption> registrars = assetRepository.findDistinctRegistrars()
                .stream()
                .map(r -> {
                    ReportFilterOptionsResponse.RegistrarOption o = new ReportFilterOptionsResponse.RegistrarOption();
                    if (r[0] instanceof Long) {
                        o.setId((Long) r[0]);
                    }
                    o.setName(r[1] != null ? r[1].toString() : "Unknown");
                    return o;
                })
                .collect(Collectors.toList());
        response.setRegisteredBy(registrars);
        return response;
    }

    private static final String[] REPORT_HEADERS = {
            "Asset Number", "Serial Number", "Zone", "Directorate", "Office",
            "Assigned To", "Device Type", "Device Model", "Device Status",
            "Registered By", "Registered At"
    };

    public String exportCsv(String search, Long deviceTypeId, String status,
            Long zoneId, String office, String userOfAsset,
            String registeredBy, LocalDate from, LocalDate to) {

        return new String(exportCsv(getFilteredAssets(search, deviceTypeId, status,
                zoneId, office, userOfAsset, registeredBy, from, to)), StandardCharsets.UTF_8);
    }

    public byte[] exportReport(String search, Long deviceTypeId, String status,
            Long zoneId, String office, String userOfAsset,
            String registeredBy, LocalDate from, LocalDate to, String format) throws IOException, DocumentException {

        List<AssetResponse> assets = getFilteredAssets(search, deviceTypeId, status,
                zoneId, office, userOfAsset, registeredBy, from, to);

        return switch (format == null ? "csv" : format.toLowerCase()) {
            case "xlsx" -> exportXlsx(assets);
            case "pdf" -> exportPdf(assets);
            default -> exportCsv(assets);
        };
    }

    private byte[] exportCsv(List<AssetResponse> assets) {
        StringBuilder csv = new StringBuilder();
        for (String header : REPORT_HEADERS) {
            if (csv.length() > 0) {
                csv.append(',');
            }
            csv.append(header);
        }
        csv.append('\n');
        for (AssetResponse a : assets) {
            csv.append(csv(a.getAssetNumber())).append(',')
               .append(csv(a.getSerialNumber())).append(',')
               .append(csv(a.getZoneName())).append(',')
               .append(csv(a.getDirectorateName())).append(',')
               .append(csv(a.getOffice())).append(',')
               .append(csv(a.getUserOfAsset())).append(',')
               .append(csv(a.getDeviceTypeName())).append(',')
               .append(csv(a.getDeviceModel())).append(',')
               .append(csv(a.getDeviceStatus() != null ? a.getDeviceStatus().name() : null)).append(',')
               .append(csv(a.getCreatedByName())).append(',')
               .append(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "")
               .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] exportXlsx(List<AssetResponse> assets) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("ICT Assets");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < REPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(REPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int r = 1;
            for (AssetResponse a : assets) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(nvl(a.getAssetNumber()));
                row.createCell(1).setCellValue(nvl(a.getSerialNumber()));
                row.createCell(2).setCellValue(nvl(a.getZoneName()));
                row.createCell(3).setCellValue(nvl(a.getDirectorateName()));
                row.createCell(4).setCellValue(nvl(a.getOffice()));
                row.createCell(5).setCellValue(nvl(a.getUserOfAsset()));
                row.createCell(6).setCellValue(nvl(a.getDeviceTypeName()));
                row.createCell(7).setCellValue(nvl(a.getDeviceModel()));
                row.createCell(8).setCellValue(a.getDeviceStatus() != null ? a.getDeviceStatus().name() : "");
                row.createCell(9).setCellValue(nvl(a.getCreatedByName()));
                row.createCell(10).setCellValue(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < REPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] exportPdf(List<AssetResponse> assets) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        BaseFont font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        BaseFont bold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);

        document.open();

        Paragraph title = new Paragraph("ICT Assets Report", new com.lowagie.text.Font(bold, 16));
        title.setSpacingAfter(16);
        document.add(title);

        PdfPTable table = new PdfPTable(REPORT_HEADERS.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(6);

        for (String header : REPORT_HEADERS) {
            table.addCell(new com.lowagie.text.pdf.PdfPCell(
                    new com.lowagie.text.Phrase(header, new com.lowagie.text.Font(bold, 8))));
        }

        for (AssetResponse a : assets) {
            table.addCell(cell(nvl(a.getAssetNumber()), font));
            table.addCell(cell(nvl(a.getSerialNumber()), font));
            table.addCell(cell(nvl(a.getZoneName()), font));
            table.addCell(cell(nvl(a.getDirectorateName()), font));
            table.addCell(cell(nvl(a.getOffice()), font));
            table.addCell(cell(nvl(a.getUserOfAsset()), font));
            table.addCell(cell(nvl(a.getDeviceTypeName()), font));
            table.addCell(cell(nvl(a.getDeviceModel()), font));
            table.addCell(cell(a.getDeviceStatus() != null ? a.getDeviceStatus().name() : "", font));
            table.addCell(cell(nvl(a.getCreatedByName()), font));
            table.addCell(cell(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "", font));
        }

        document.add(table);
        document.close();
        return out.toByteArray();
    }

    private com.lowagie.text.pdf.PdfPCell cell(String text, BaseFont font) {
        return new com.lowagie.text.pdf.PdfPCell(
                new com.lowagie.text.Phrase(text, new com.lowagie.text.Font(font, 8)));
    }

    private String nvl(String value) {
        return value != null ? value : "";
    }

    // ---------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------

    private List<AssetResponse> getFilteredAssets(String search, Long deviceTypeId, String status,
            Long zoneId, String office, String userOfAsset,
            String registeredBy, LocalDate from, LocalDate to) {

        AssetService.SearchParams sp = AssetService.normalizeSearch(search);
        String termLike = sp.term() != null ? sp.term() : "%%";
        DeviceStatus statusFilter = parseStatus(status);
        String officeLike = likeOrMatchAll(office);
        String userOfAssetLike = likeOrMatchAll(userOfAsset);
        String registeredByLike = likeOrMatchAll(registeredBy);
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : LocalDateTime.of(1, 1, 1, 0, 0);
        LocalDateTime toDt = to != null ? to.plusDays(1).atStartOfDay() : LocalDateTime.of(9999, 12, 31, 23, 59, 59);

        return assetRepository.findForReport(
                        termLike, sp.statusOn(), sp.statuses(),
                        deviceTypeId, statusFilter,
                        zoneId, officeLike, userOfAssetLike, registeredByLike, fromDt, toDt)
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());
    }

    private ReportSummaryResponse buildSummary(List<AssetResponse> assets) {
        ReportSummaryResponse s = new ReportSummaryResponse();
        s.setTotalAssets(assets.size());
        s.setActiveAssets(assets.stream().filter(a -> a.getDeviceStatus() == DeviceStatus.WORKING).count());
        s.setDefectiveAssets(assets.stream().filter(a -> a.getDeviceStatus() == DeviceStatus.NOT_WORKING).count());
        return s;
    }

    private boolean isGrouped(String groupBy) {
        return groupBy != null && !groupBy.isBlank()
                && !groupBy.equals("overview") && !groupBy.equals("registration");
    }

    private List<ReportResponse.ReportItem> buildGroupedItems(List<AssetResponse> assets, String groupBy) {
        Map<String, List<AssetResponse>> grouped = assets.stream()
                .collect(Collectors.groupingBy(a -> groupKey(a, groupBy)));

        return grouped.entrySet().stream()
                .map(entry -> {
                    ReportResponse.ReportItem item = new ReportResponse.ReportItem();
                    item.setName(entry.getKey());
                    List<AssetResponse> groupAssets = entry.getValue();
                    item.setCount(groupAssets.size());
                    item.setWorking(groupAssets.stream().filter(a -> a.getDeviceStatus() == DeviceStatus.WORKING).count());
                    item.setNotWorking(groupAssets.stream().filter(a -> a.getDeviceStatus() == DeviceStatus.NOT_WORKING).count());
                    groupAssets.stream().findFirst().ifPresent(first -> {
                        switch (groupBy) {
                            case "by-zone" -> item.setId(first.getZoneId());
                            case "by-device-type" -> item.setId(first.getDeviceTypeId());
                            default -> item.setId(null);
                        }
                    });
                    item.setAssets(new ArrayList<>(groupAssets));
                    return item;
                })
                .sorted(Comparator.comparing(ReportResponse.ReportItem::getCount).reversed()
                        .thenComparing(ReportResponse.ReportItem::getName))
                .collect(Collectors.toList());
    }

    private String groupKey(AssetResponse a, String groupBy) {
        return switch (groupBy) {
            case "by-zone" -> a.getZoneName() != null ? a.getZoneName() : "Unknown";
            case "by-office" -> a.getOffice() != null ? a.getOffice() : "Unknown";
            case "by-device-type" -> a.getDeviceTypeName() != null ? a.getDeviceTypeName() : "Unknown";
            case "by-status" -> a.getDeviceStatus() != null ? a.getDeviceStatus().name() : "Unknown";
            case "by-user" -> a.getUserOfAsset() != null ? a.getUserOfAsset() : "Unknown";
            default -> "Unknown";
        };
    }

    private DeviceStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return DeviceStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String blankToLike(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "%" + value.trim().toLowerCase() + "%";
    }

    /**
     * LIKE pattern that, when unfiltered, becomes {@code %%} (matches everything)
     * instead of {@code null}. This avoids PostgreSQL's "could not determine data
     * type of parameter" error, which occurs when a LIKE bound-parameter is null.
     */
    private String likeOrMatchAll(String value) {
        if (value == null || value.isBlank()) {
            return "%%";
        }
        return "%" + value.trim().toLowerCase() + "%";
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
