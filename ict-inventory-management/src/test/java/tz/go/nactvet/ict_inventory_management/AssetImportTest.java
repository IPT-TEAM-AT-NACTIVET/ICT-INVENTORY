package tz.go.nactvet.ict_inventory_management;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.dto.CsvImportResult;
import tz.go.nactvet.ict_inventory_management.entity.DeviceType;
import tz.go.nactvet.ict_inventory_management.entity.Zone;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;
import tz.go.nactvet.ict_inventory_management.repository.DeviceTypeRepository;
import tz.go.nactvet.ict_inventory_management.repository.ZoneRepository;
import tz.go.nactvet.ict_inventory_management.service.AssetService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AssetImportTest {

    @Autowired
    private AssetService assetService;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM assets");
        jdbcTemplate.update("DELETE FROM users WHERE email IS NOT NULL AND email <> 'admin@nactvet.go.tz'");
        zoneRepository.findByName("Central Zone").orElseGet(() -> zoneRepository.save(zone("Central Zone")));
        deviceTypeRepository.findByName("Laptop").orElseGet(() -> deviceTypeRepository.save(deviceType("Laptop")));
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

    @Test
    void realisticHeaders_importsRowAndMapsRoomAndAssignedTo() {
        String csv = String.join("\n",
                "Asset Number,Serial Number,Zone,Directorate,Office,Assigned To,Device Type/Name,Device Model,Device Status",
                "NCT-XLS-1,SN-XLS-1,Central Zone,,Room 101,John Mkwizu,Laptop,Dell Latitude 5520,WORKING");

        CsvImportResult result = assetService.importCsv(csv, null);

        assertThat(result.getImported()).isEqualTo(1);
        assertThat(result.getErrors()).isEmpty();

        var saved = assetRepository.findBySerialNumber("SN-XLS-1").orElseThrow();
        assertThat(saved.getAssetNumber()).isEqualTo("NCT-XLS-1");
        assertThat(saved.getOffice()).isEqualTo("Room 101");
        assertThat(saved.getUserOfAsset()).isEqualTo("John Mkwizu");
        assertThat(saved.getDeviceModel()).isEqualTo("Dell Latitude 5520");
        assertThat(saved.getDeviceType().getName()).isEqualTo("Laptop");
    }

    @Test
    void optionalColumnsMissing_doesNotThrow() {
        String csv = String.join("\n",
                "Zone,Device Type,Device Name,Device Status",
                "Central Zone,Laptop,Dell Latitude 5520,WORKING");

        CsvImportResult result = assetService.importCsv(csv, null);

        assertThat(result.getImported()).isEqualTo(1);
        assertThat(result.getErrors()).isEmpty();

        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM assets WHERE device_model = ?", Long.class, "Dell Latitude 5520");
        assertThat(count).isEqualTo(1);
        String office = jdbcTemplate.queryForObject(
                "SELECT office FROM assets WHERE device_model = ?", String.class, "Dell Latitude 5520");
        assertThat(office).isNull();
    }

    @Test
    void missingZoneOrType_reportsMissingHeader() {
        String csv = String.join("\n",
                "Asset Number,Device Name,Device Model,Device Status",
                "NCT-XLS-2,Laptop,Dell Latitude 5520,WORKING");

        CsvImportResult result = assetService.importCsv(csv, null);

        assertThat(result.getImported()).isZero();
        assertThat(result.getErrors()).anySatisfy(e -> {
            assertThat(e.getMessage()).contains("missing the required header");
        });
    }

    @Test
    void zoneWithNonBreakingSpaces_matchesExistingZone() {
        String csv = String.join("\n",
                "Zone,Device Type,Device Name,Device Status",
                "Central\u00A0Zone,Laptop,Dell NBSPSeries,WORKING");

        CsvImportResult result = assetService.importCsv(csv, null);

        assertThat(result.getImported()).isEqualTo(1);
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void zoneMatchingIsCaseInsensitive() {
        String csv = String.join("\n",
                "Zone,Device Type,Device Name,Device Status",
                "central zone,Laptop,Dell CaseSeries,WORKING");

        CsvImportResult result = assetService.importCsv(csv, null);

        assertThat(result.getImported()).isEqualTo(1);
        assertThat(result.getErrors()).isEmpty();
    }
}