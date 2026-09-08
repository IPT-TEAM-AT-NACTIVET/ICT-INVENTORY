package tz.go.nactvet.ict_inventory_management;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.ReportFilterOptionsResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportResponse;
import tz.go.nactvet.ict_inventory_management.dto.ReportSummaryResponse;
import tz.go.nactvet.ict_inventory_management.entity.DeviceType;
import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.entity.Zone;
import tz.go.nactvet.ict_inventory_management.enums.Role;
import tz.go.nactvet.ict_inventory_management.repository.DeviceTypeRepository;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;
import tz.go.nactvet.ict_inventory_management.repository.ZoneRepository;
import tz.go.nactvet.ict_inventory_management.service.ReportService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Zone zone;
    private DeviceType laptopType;

    private Long aliceId;
    private Long bobId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM assets");
        jdbcTemplate.update("DELETE FROM users WHERE email IS NOT NULL AND email <> 'admin@nactvet.go.tz'");

        zone = zoneRepository.findByName("Central Zone").orElseGet(() -> zoneRepository.save(zone("Central Zone")));
        laptopType = deviceTypeRepository.findByName("Laptop").orElseGet(() -> deviceTypeRepository.save(deviceType("Laptop")));

        User alice = userRepository.save(user("Alice Mwangi", "alice@nactvet.go.tz"));
        User bob = userRepository.save(user("Bob Johnson", "bob@nactvet.go.tz"));
        aliceId = alice.getId();
        bobId = bob.getId();

        LocalDateTime base = LocalDateTime.of(2026, 1, 15, 10, 0);
        insertAsset("NCT-001", "SN001", "Laptop-001", "John Doe", aliceId, "A100", "WORKING", base);
        insertAsset("NCT-002", "SN002", "Laptop-002", "John Doe", aliceId, "A101", "NOT_WORKING", base.plusMonths(1));
        insertAsset("NCT-003", "SN003", "Laptop-003", "Jane Smith", bobId, "B200", "WORKING", base.plusMonths(2));
        insertAsset("NCT-004", "SN004", "Laptop-004", "John Doe", aliceId, null, "WORKING", base.plusMonths(3));
        insertAsset("NCT-005", "SN005", "Laptop-005", "Jane Smith", bobId, "A100", "WORKING", base.plusMonths(4));
    }

    private Zone zone(String name) {
        Zone z = new Zone();
        z.setName(name);
        z.setStatus("ACTIVE");
        return z;
    }

    private DeviceType deviceType(String name) {
        DeviceType d = new DeviceType();
        d.setName(name);
        return d;
    }

    private User user(String fullName, String email) {
        User u = new User();
        u.setEmployeeId("NCT-EMP-" + email.hashCode());
        u.setFullName(fullName);
        u.setUsername(email);
        u.setEmail(email);
        u.setPassword("Password@123");
        u.setRole(Role.ADMIN);
        return u;
    }

    private void insertAsset(String assetNumber, String serialNumber, String model, String userOfAsset,
            Long createdById, String office, String deviceStatus, LocalDateTime createdAt) {
        jdbcTemplate.update(
                "INSERT INTO assets (asset_number, serial_number, device_model, device_type_id, user_of_asset,"
                        + " created_by, zone_id, office, device_status, created_at, updated_at)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                assetNumber, serialNumber, model, laptopType.getId(), userOfAsset,
                createdById, zone.getId(), office, deviceStatus, createdAt, createdAt);
    }

    // -------------------------------------------------------------------

    private ReportResponse data(String search, String groupBy) {
        return reportService.getReportData(search, null, null, null, null, null, null, null, null, groupBy);
    }

    @Test
    void noFilter_summaryTotalsMatchDatabase() {
        ReportResponse r = data(null, "overview");
        ReportSummaryResponse s = r.getSummary();

        assertThat(r.getAssets()).hasSize(5);
        assertThat(s.getTotalAssets()).isEqualTo(5);
        assertThat(s.getActiveAssets()).isEqualTo(4);
        assertThat(s.getDefectiveAssets()).isEqualTo(1);
        assertThat(r.getItems()).isEmpty();
    }

    @Test
    void searchNotWorking_returnsOnlyNotWorkingAssets() {
        ReportResponse r = data("not working", "overview");

        assertThat(r.getAssets()).hasSize(1);
        assertThat(r.getAssets().get(0).getAssetNumber()).isEqualTo("NCT-002");
        assertThat(r.getSummary().getTotalAssets()).isEqualTo(1);
        assertThat(r.getSummary().getDefectiveAssets()).isEqualTo(1);
        assertThat(r.getSummary().getActiveAssets()).isZero();
    }

    @Test
    void searchWorking_returnsOnlyWorkingAssets() {
        ReportResponse r = data("working", "overview");

        assertThat(r.getAssets()).hasSize(4);
        assertThat(r.getSummary().getActiveAssets()).isEqualTo(4);
        assertThat(r.getSummary().getDefectiveAssets()).isZero();
    }

    @Test
    void searchBySerialNumber_returnsMatchingAsset() {
        ReportResponse r = data("SN003", "overview");

        assertThat(r.getAssets()).hasSize(1);
        assertThat(r.getAssets().get(0).getAssetNumber()).isEqualTo("NCT-003");
    }

    @Test
    void filterByStatus_workingOnly() {
        ReportResponse r = reportService.getReportData(null, null, "WORKING", null, null, null, null, null, null, "overview");

        assertThat(r.getAssets()).hasSize(4);
        assertThat(r.getSummary().getActiveAssets()).isEqualTo(4);
        assertThat(r.getSummary().getDefectiveAssets()).isZero();
    }

    @Test
    void filterByOffice_returnsMatchingAssets() {
        ReportResponse r = reportService.getReportData(null, null, null, null, "A100", null, null, null, null, "overview");

        assertThat(r.getAssets()).hasSize(2);
        assertThat(r.getSummary().getTotalAssets()).isEqualTo(2);
    }

    @Test
    void optionalFilters_blankOrNull_areCompletelyIgnored() {
        ReportResponse r = reportService.getReportData(null, null, null, null, "", "", "", null, null, "overview");

        assertThat(r.getAssets()).hasSize(5);
        assertThat(r.getSummary().getTotalAssets()).isEqualTo(5);
    }

    @Test
    void multiFilter_allSelectedConditionsMustBeTrue() {
        ReportResponse r = reportService.getReportData(null, laptopType.getId(), "WORKING", zone.getId(), "A100", null,
                "Alice Mwangi", null, null, "overview");

        assertThat(r.getAssets()).hasSize(1);
        assertThat(r.getAssets().get(0).getAssetNumber()).isEqualTo("NCT-001");
        assertThat(r.getSummary().getTotalAssets()).isEqualTo(1);
        assertThat(r.getSummary().getActiveAssets()).isEqualTo(1);
    }

    @Test
    void registeredByFilter_combinesWithStatusAndZone() {
        ReportResponse r = reportService.getReportData(null, null, "WORKING", zone.getId(), null, null,
                "Alice Mwangi", null, null, "overview");

        assertThat(r.getAssets()).hasSize(2);
        assertThat(r.getAssets()).extracting(a -> a.getAssetNumber())
                .containsExactlyInAnyOrder("NCT-001", "NCT-004");
    }

    @Test
    void dateRange_singleBound_appliesOnlyTheProvidedBound() {
        ReportResponse r = reportService.getReportData(null, null, null, null, null, null, null,
                LocalDate.of(2026, 3, 1), null, "overview");

        assertThat(r.getAssets()).hasSize(3); // NCT-003, NCT-004, NCT-005
        assertThat(r.getAssets()).extracting(a -> a.getAssetNumber())
                .containsExactlyInAnyOrder("NCT-003", "NCT-004", "NCT-005");
    }

    @Test
    void filterByZoneAndStatus_combine() {
        ReportResponse r = reportService.getReportData(null, null, "WORKING", zone.getId(), null, null, null, null, null, "overview");

        // All 5 assets are in this zone; filter to WORKING only.
        assertThat(r.getAssets()).hasSize(4);
        for (var a : r.getAssets()) {
            assertThat(a.getDeviceStatus().name()).isEqualTo("WORKING");
        }
    }

    @Test
    void dateRangeFilter_returnsAssetsInsideRange() {
        ReportResponse r = reportService.getReportData(null, null, null, null, null, null, null,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 4, 30), "overview");

        assertThat(r.getAssets()).hasSize(2);
        assertThat(r.getSummary().getTotalAssets()).isEqualTo(2);
    }

    @Test
    void groupedByStatus_itemsMatch() {
        ReportResponse r = data(null, "by-status");

        assertThat(r.getItems()).hasSize(2);
        ReportResponse.ReportItem working = itemByName(r, "WORKING");
        assertThat(working.getCount()).isEqualTo(4);
        assertThat(working.getWorking()).isEqualTo(4);
        assertThat(working.getNotWorking()).isZero();
        assertThat(working.getAssets()).hasSize(4);

        ReportResponse.ReportItem notWorking = itemByName(r, "NOT_WORKING");
        assertThat(notWorking.getCount()).isEqualTo(1);
        assertThat(notWorking.getNotWorking()).isEqualTo(1);
        assertThat(notWorking.getAssets()).hasSize(1);
    }

    @Test
    void groupedByZone_singleZone() {
        ReportResponse r = data(null, "by-zone");

        assertThat(r.getItems()).hasSize(1);
        ReportResponse.ReportItem central = r.getItems().get(0);
        assertThat(central.getName()).isEqualTo("Central Zone");
        assertThat(central.getCount()).isEqualTo(5);
        assertThat(central.getAssets()).hasSize(5);
    }

    @Test
    void groupedByOffice_groupsCorrectly() {
        ReportResponse r = data(null, "by-office");

        assertThat(r.getItems()).hasSize(4); // A100(2), A101(1), B200(1), Unknown(1)
        ReportResponse.ReportItem a100 = r.getItems().stream()
                .filter(i -> i.getName().equals("A100"))
                .findFirst().orElseThrow();
        assertThat(a100.getCount()).isEqualTo(2);
        assertThat(a100.getWorking()).isEqualTo(2);
        assertThat(a100.getAssets()).hasSize(2);
    }

    @Test
    void groupedByUser_fullNamesUsed() {
        ReportResponse r = data(null, "by-user");

        assertThat(r.getItems()).hasSize(2);
        ReportResponse.ReportItem john = itemByName(r, "John Doe");
        assertThat(john.getCount()).isEqualTo(3);
        assertThat(john.getWorking()).isEqualTo(2);
        assertThat(john.getNotWorking()).isEqualTo(1);
        assertThat(itemByName(r, "Jane Smith").getCount()).isEqualTo(2);
    }

    @Test
    void filterOptions_derivedFromAssets() {
        ReportFilterOptionsResponse opts = reportService.getFilterOptions();

        assertThat(opts.getOffices()).containsExactly("A100", "A101", "B200");
        assertThat(opts.getRegisteredBy()).hasSize(2);
    }

    @Test
    void exportCsv_matchesFilteredAssets() {
        String csv = reportService.exportCsv("not working", null, null, null, null, null, null, null, null);

        String[] lines = csv.split("\n");
        assertThat(lines).hasSize(2); // header + 1 asset
        assertThat(lines[0]).contains("Asset Number").contains("Registered By,Registered At");
        assertThat(lines[1]).contains("NCT-002");
    }

    @Test
    void exportCsv_containsRegisteredByAndAtColumns() {
        String csv = reportService.exportCsv(null, null, null, null, null, null, null, null, null);

        String[] lines = csv.split("\n");
        assertThat(lines).hasSize(6); // header + 5 assets
        assertThat(lines[0]).contains("Registered By").contains("Registered At");
    }

    @Test
    void exportXlsx_producesValidWorkbook() throws Exception {
        byte[] bytes = reportService.exportReport(null, null, null, null, null, null, null, null, null, "xlsx");

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb =
                new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(bytes))) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheet("ICT Assets");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(6); // header + 5 assets
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Asset Number");
        }
    }

    @Test
    void exportPdf_producesValidPdf() throws Exception {
        byte[] bytes = reportService.exportReport(null, null, null, null, null, null, null, null, null, "pdf");

        assertThat(bytes).isNotEmpty();
        // PDF header magic
        assertThat(new String(bytes, 0, 5, java.nio.charset.StandardCharsets.UTF_8)).isEqualTo("%PDF-");
    }

    @Test
    void exportReport_csvIsDefaultWhenFormatUnknown() throws Exception {
        byte[] bytes = reportService.exportReport(null, null, null, null, null, null, null, null, null, "doc");

        String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        assertThat(text).startsWith("Asset Number,Serial Number,Zone");
    }

    private ReportResponse.ReportItem itemByName(ReportResponse r, String name) {
        return r.getItems().stream().filter(i -> i.getName().equals(name)).findFirst().orElseThrow();
    }
}