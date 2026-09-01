package tz.go.nactvet.ict_inventory_management.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tz.go.nactvet.ict_inventory_management.entity.Asset;
import tz.go.nactvet.ict_inventory_management.entity.DeviceType;
import tz.go.nactvet.ict_inventory_management.entity.Section;
import tz.go.nactvet.ict_inventory_management.entity.Unit;
import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.entity.Zone;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.OwnershipType;
import tz.go.nactvet.ict_inventory_management.enums.Role;
import tz.go.nactvet.ict_inventory_management.enums.VerificationStatus;
import tz.go.nactvet.ict_inventory_management.repository.AssetRepository;
import tz.go.nactvet.ict_inventory_management.repository.DeviceTypeRepository;
import tz.go.nactvet.ict_inventory_management.repository.SectionRepository;
import tz.go.nactvet.ict_inventory_management.repository.UnitRepository;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;
import tz.go.nactvet.ict_inventory_management.repository.ZoneRepository;

/**
 * Seeds STAFF accounts and ICT assets after the NACTVET master data is in place
 * ({@link MasterDataInitializer} is an ApplicationRunner and runs before every
 * CommandLineRunner).
 *
 * <p>Idempotently creates:</p>
 * <ul>
 *   <li>11 device types</li>
 *   <li>30 staff users (shared default password {@code Password@123}), assigned
 *       to the official sections / units created by {@link MasterDataInitializer}</li>
 *   <li>150 assets (5 per staff user) across the 8 zones</li>
 * </ul>
 *
 * <p>All staff share the same BCrypt hash for {@code Password@123}; this is a
 * well-known dev/test default and must be changed in production.</p>
 */
@Component
public class DataSeedInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeedInitializer.class);

    /** BCrypt hash of the shared staff password {@code Password@123}. */
    private static final String STAFF_PASSWORD_HASH =
            "$2a$10$I8Dj5GFOkhr//MiDv1Sj4.KNymXymuNSluIdpteuqyayEVV.MmPb2";

    private static final String[] SECTIONS = {
            "Registration and Accreditation Section",
            "Labour Market Analysis and Curriculum Development Section", "Admission Section",
            "Examinations and Certification Section", "Academic Quality Audit Section",
            "Compliance and Enforcement Section", "Human Resource Management and Administration Section",
            "Planning, Monitoring and Evaluation Section"
    };

    private static final String[] UNITS = {
            "Information and Communication Technology Unit", "Finance and Accounts Unit",
            "Procurement Management Unit", "Internal Audit Unit", "Communications and Marketing Unit",
            "Legal Services Unit"
    };

    private static final String[] ZONES = {
            "Eastern Zone", "Central Zone", "Northern Zone", "Zanzibar Zone", "Western Zone",
            "Lake Zone", "Southern Highlands Zone", "Southern Zone"
    };

    // Asset columns: assetNumber, serialNumber, deviceName, ownershipType,
    // deviceStatus, verificationStatus, office, deviceTypeName, staffEmail, zoneName
    private static final String[][] ASSETS = {
            {"NCT-ICT-001-01", "NCTSER-001-01", "Laptop - 01-01", "OFFICE", "ACTIVE", "VERIFIED", "A2", "Laptop", "amani.juma@example.com", "Central Zone"},
            {"NCT-ICT-001-02", "NCTSER-001-02", "Desktop Computer - 01-02", "PERSONAL", "ACTIVE", "VERIFIED", "B3", "Desktop Computer", "amani.juma@example.com", "Northern Zone"},
            {"NCT-ICT-001-03", "NCTSER-001-03", "Projector - 01-03", "OFFICE", "ACTIVE", "VERIFIED", "4", "Projector", "amani.juma@example.com", "Zanzibar Zone"},
            {"NCT-ICT-001-04", "NCTSER-001-04", "Photocopier - 01-04", "PERSONAL", "ACTIVE", "PENDING", "ICT-5", "Photocopier", "amani.juma@example.com", "Western Zone"},
            {"NCT-ICT-001-05", "NCTSER-001-05", "Printer - 01-05", "OFFICE", "ACTIVE", "VERIFIED", "F6", "Printer", "amani.juma@example.com", "Lake Zone"},
            {"NCT-ICT-002-01", "NCTSER-002-01", "Desktop Computer - 02-01", "OFFICE", "ACTIVE", "VERIFIED", "A3", "Desktop Computer", "neema.hassan@example.com", "Northern Zone"},
            {"NCT-ICT-002-02", "NCTSER-002-02", "Projector - 02-02", "PERSONAL", "ACTIVE", "VERIFIED", "B4", "Projector", "neema.hassan@example.com", "Zanzibar Zone"},
            {"NCT-ICT-002-03", "NCTSER-002-03", "Photocopier - 02-03", "OFFICE", "ACTIVE", "PENDING", "5", "Photocopier", "neema.hassan@example.com", "Western Zone"},
            {"NCT-ICT-002-04", "NCTSER-002-04", "Printer - 02-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-6", "Printer", "neema.hassan@example.com", "Lake Zone"},
            {"NCT-ICT-002-05", "NCTSER-002-05", "Scanner - 02-05", "OFFICE", "ACTIVE", "VERIFIED", "G7", "Scanner", "neema.hassan@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-003-01", "NCTSER-003-01", "Projector - 03-01", "OFFICE", "ACTIVE", "VERIFIED", "A4", "Projector", "baraka.mushi@example.com", "Zanzibar Zone"},
            {"NCT-ICT-003-02", "NCTSER-003-02", "Photocopier - 03-02", "PERSONAL", "ACTIVE", "PENDING", "B5", "Photocopier", "baraka.mushi@example.com", "Western Zone"},
            {"NCT-ICT-003-03", "NCTSER-003-03", "Printer - 03-03", "OFFICE", "ACTIVE", "VERIFIED", "6", "Printer", "baraka.mushi@example.com", "Lake Zone"},
            {"NCT-ICT-003-04", "NCTSER-003-04", "Scanner - 03-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-7", "Scanner", "baraka.mushi@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-003-05", "NCTSER-003-05", "Monitor - 03-05", "OFFICE", "ACTIVE", "VERIFIED", "H8", "Monitor", "baraka.mushi@example.com", "Southern Zone"},
            {"NCT-ICT-004-01", "NCTSER-004-01", "Photocopier - 04-01", "OFFICE", "ACTIVE", "PENDING", "A5", "Photocopier", "rehema.said@example.com", "Western Zone"},
            {"NCT-ICT-004-02", "NCTSER-004-02", "Printer - 04-02", "PERSONAL", "ACTIVE", "VERIFIED", "B6", "Printer", "rehema.said@example.com", "Lake Zone"},
            {"NCT-ICT-004-03", "NCTSER-004-03", "Scanner - 04-03", "OFFICE", "ACTIVE", "VERIFIED", "7", "Scanner", "rehema.said@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-004-04", "NCTSER-004-04", "Monitor - 04-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-8", "Monitor", "rehema.said@example.com", "Southern Zone"},
            {"NCT-ICT-004-05", "NCTSER-004-05", "Network Switch - 04-05", "OFFICE", "ACTIVE", "PENDING", "I9", "Network Switch", "rehema.said@example.com", "Eastern Zone"},
            {"NCT-ICT-005-01", "NCTSER-005-01", "Printer - 05-01", "OFFICE", "ACTIVE", "VERIFIED", "A6", "Printer", "daniel.joseph@example.com", "Lake Zone"},
            {"NCT-ICT-005-02", "NCTSER-005-02", "Scanner - 05-02", "PERSONAL", "ACTIVE", "VERIFIED", "B7", "Scanner", "daniel.joseph@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-005-03", "NCTSER-005-03", "Monitor - 05-03", "OFFICE", "ACTIVE", "VERIFIED", "8", "Monitor", "daniel.joseph@example.com", "Southern Zone"},
            {"NCT-ICT-005-04", "NCTSER-005-04", "Network Switch - 05-04", "PERSONAL", "ACTIVE", "PENDING", "ICT-9", "Network Switch", "daniel.joseph@example.com", "Eastern Zone"},
            {"NCT-ICT-005-05", "NCTSER-005-05", "Wireless Access Point - 05-05", "OFFICE", "ACTIVE", "VERIFIED", "J10", "Wireless Access Point", "daniel.joseph@example.com", "Central Zone"},
            {"NCT-ICT-006-01", "NCTSER-006-01", "Scanner - 06-01", "OFFICE", "ACTIVE", "VERIFIED", "A7", "Scanner", "grace.peter@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-006-02", "NCTSER-006-02", "Monitor - 06-02", "PERSONAL", "ACTIVE", "VERIFIED", "B8", "Monitor", "grace.peter@example.com", "Southern Zone"},
            {"NCT-ICT-006-03", "NCTSER-006-03", "Network Switch - 06-03", "OFFICE", "ACTIVE", "PENDING", "9", "Network Switch", "grace.peter@example.com", "Eastern Zone"},
            {"NCT-ICT-006-04", "NCTSER-006-04", "Wireless Access Point - 06-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-10", "Wireless Access Point", "grace.peter@example.com", "Central Zone"},
            {"NCT-ICT-006-05", "NCTSER-006-05", "UPS - 06-05", "OFFICE", "DEFECTIVE", "VERIFIED", "K11", "UPS", "grace.peter@example.com", "Northern Zone"},
            {"NCT-ICT-007-01", "NCTSER-007-01", "Monitor - 07-01", "OFFICE", "ACTIVE", "VERIFIED", "A8", "Monitor", "ibrahim.salum@example.com", "Southern Zone"},
            {"NCT-ICT-007-02", "NCTSER-007-02", "Network Switch - 07-02", "PERSONAL", "ACTIVE", "PENDING", "B9", "Network Switch", "ibrahim.salum@example.com", "Eastern Zone"},
            {"NCT-ICT-007-03", "NCTSER-007-03", "Wireless Access Point - 07-03", "OFFICE", "ACTIVE", "VERIFIED", "10", "Wireless Access Point", "ibrahim.salum@example.com", "Central Zone"},
            {"NCT-ICT-007-04", "NCTSER-007-04", "UPS - 07-04", "PERSONAL", "DEFECTIVE", "VERIFIED", "ICT-1", "UPS", "ibrahim.salum@example.com", "Northern Zone"},
            {"NCT-ICT-007-05", "NCTSER-007-05", "Server - 07-05", "OFFICE", "ACTIVE", "VERIFIED", "L12", "Server", "ibrahim.salum@example.com", "Zanzibar Zone"},
            {"NCT-ICT-008-01", "NCTSER-008-01", "Network Switch - 08-01", "OFFICE", "ACTIVE", "PENDING", "A9", "Network Switch", "joyce.emmanuel@example.com", "Eastern Zone"},
            {"NCT-ICT-008-02", "NCTSER-008-02", "Wireless Access Point - 08-02", "PERSONAL", "ACTIVE", "VERIFIED", "B10", "Wireless Access Point", "joyce.emmanuel@example.com", "Central Zone"},
            {"NCT-ICT-008-03", "NCTSER-008-03", "UPS - 08-03", "OFFICE", "DEFECTIVE", "VERIFIED", "11", "UPS", "joyce.emmanuel@example.com", "Northern Zone"},
            {"NCT-ICT-008-04", "NCTSER-008-04", "Server - 08-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-2", "Server", "joyce.emmanuel@example.com", "Zanzibar Zone"},
            {"NCT-ICT-008-05", "NCTSER-008-05", "Laptop - 08-05", "OFFICE", "ACTIVE", "PENDING", "M13", "Laptop", "joyce.emmanuel@example.com", "Western Zone"},
            {"NCT-ICT-009-01", "NCTSER-009-01", "Wireless Access Point - 09-01", "OFFICE", "ACTIVE", "VERIFIED", "A10", "Wireless Access Point", "kelvin.michael@example.com", "Central Zone"},
            {"NCT-ICT-009-02", "NCTSER-009-02", "UPS - 09-02", "PERSONAL", "DEFECTIVE", "VERIFIED", "B11", "UPS", "kelvin.michael@example.com", "Northern Zone"},
            {"NCT-ICT-009-03", "NCTSER-009-03", "Server - 09-03", "OFFICE", "ACTIVE", "VERIFIED", "12", "Server", "kelvin.michael@example.com", "Zanzibar Zone"},
            {"NCT-ICT-009-04", "NCTSER-009-04", "Laptop - 09-04", "PERSONAL", "ACTIVE", "PENDING", "ICT-3", "Laptop", "kelvin.michael@example.com", "Western Zone"},
            {"NCT-ICT-009-05", "NCTSER-009-05", "Desktop Computer - 09-05", "OFFICE", "ACTIVE", "VERIFIED", "N14", "Desktop Computer", "kelvin.michael@example.com", "Lake Zone"},
            {"NCT-ICT-010-01", "NCTSER-010-01", "UPS - 10-01", "OFFICE", "DEFECTIVE", "VERIFIED", "A11", "UPS", "sophia.george@example.com", "Northern Zone"},
            {"NCT-ICT-010-02", "NCTSER-010-02", "Server - 10-02", "PERSONAL", "ACTIVE", "VERIFIED", "B12", "Server", "sophia.george@example.com", "Zanzibar Zone"},
            {"NCT-ICT-010-03", "NCTSER-010-03", "Laptop - 10-03", "OFFICE", "ACTIVE", "PENDING", "13", "Laptop", "sophia.george@example.com", "Western Zone"},
            {"NCT-ICT-010-04", "NCTSER-010-04", "Desktop Computer - 10-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-4", "Desktop Computer", "sophia.george@example.com", "Lake Zone"},
            {"NCT-ICT-010-05", "NCTSER-010-05", "Projector - 10-05", "OFFICE", "ACTIVE", "VERIFIED", "O15", "Projector", "sophia.george@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-011-01", "NCTSER-011-01", "Server - 11-01", "OFFICE", "ACTIVE", "VERIFIED", "A12", "Server", "hassan.omari@example.com", "Zanzibar Zone"},
            {"NCT-ICT-011-02", "NCTSER-011-02", "Laptop - 11-02", "PERSONAL", "ACTIVE", "PENDING", "B13", "Laptop", "hassan.omari@example.com", "Western Zone"},
            {"NCT-ICT-011-03", "NCTSER-011-03", "Desktop Computer - 11-03", "OFFICE", "ACTIVE", "VERIFIED", "14", "Desktop Computer", "hassan.omari@example.com", "Lake Zone"},
            {"NCT-ICT-011-04", "NCTSER-011-04", "Projector - 11-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-5", "Projector", "hassan.omari@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-011-05", "NCTSER-011-05", "Photocopier - 11-05", "OFFICE", "ACTIVE", "VERIFIED", "P1", "Photocopier", "hassan.omari@example.com", "Southern Zone"},
            {"NCT-ICT-012-01", "NCTSER-012-01", "Laptop - 12-01", "OFFICE", "ACTIVE", "PENDING", "A13", "Laptop", "esther.william@example.com", "Western Zone"},
            {"NCT-ICT-012-02", "NCTSER-012-02", "Desktop Computer - 12-02", "PERSONAL", "ACTIVE", "VERIFIED", "B14", "Desktop Computer", "esther.william@example.com", "Lake Zone"},
            {"NCT-ICT-012-03", "NCTSER-012-03", "Projector - 12-03", "OFFICE", "ACTIVE", "VERIFIED", "15", "Projector", "esther.william@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-012-04", "NCTSER-012-04", "Photocopier - 12-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-6", "Photocopier", "esther.william@example.com", "Southern Zone"},
            {"NCT-ICT-012-05", "NCTSER-012-05", "Printer - 12-05", "OFFICE", "ACTIVE", "PENDING", "Q2", "Printer", "esther.william@example.com", "Eastern Zone"},
            {"NCT-ICT-013-01", "NCTSER-013-01", "Desktop Computer - 13-01", "OFFICE", "ACTIVE", "VERIFIED", "A14", "Desktop Computer", "patrick.simon@example.com", "Lake Zone"},
            {"NCT-ICT-013-02", "NCTSER-013-02", "Projector - 13-02", "PERSONAL", "ACTIVE", "VERIFIED", "B15", "Projector", "patrick.simon@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-013-03", "NCTSER-013-03", "Photocopier - 13-03", "OFFICE", "ACTIVE", "VERIFIED", "16", "Photocopier", "patrick.simon@example.com", "Southern Zone"},
            {"NCT-ICT-013-04", "NCTSER-013-04", "Printer - 13-04", "PERSONAL", "ACTIVE", "PENDING", "ICT-7", "Printer", "patrick.simon@example.com", "Eastern Zone"},
            {"NCT-ICT-013-05", "NCTSER-013-05", "Scanner - 13-05", "OFFICE", "ACTIVE", "VERIFIED", "R3", "Scanner", "patrick.simon@example.com", "Central Zone"},
            {"NCT-ICT-014-01", "NCTSER-014-01", "Projector - 14-01", "OFFICE", "ACTIVE", "VERIFIED", "A15", "Projector", "mariam.ali@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-014-02", "NCTSER-014-02", "Photocopier - 14-02", "PERSONAL", "ACTIVE", "VERIFIED", "B16", "Photocopier", "mariam.ali@example.com", "Southern Zone"},
            {"NCT-ICT-014-03", "NCTSER-014-03", "Printer - 14-03", "OFFICE", "ACTIVE", "PENDING", "17", "Printer", "mariam.ali@example.com", "Eastern Zone"},
            {"NCT-ICT-014-04", "NCTSER-014-04", "Scanner - 14-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-8", "Scanner", "mariam.ali@example.com", "Central Zone"},
            {"NCT-ICT-014-05", "NCTSER-014-05", "Monitor - 14-05", "OFFICE", "ACTIVE", "VERIFIED", "S4", "Monitor", "mariam.ali@example.com", "Northern Zone"},
            {"NCT-ICT-015-01", "NCTSER-015-01", "Photocopier - 15-01", "OFFICE", "ACTIVE", "VERIFIED", "A16", "Photocopier", "david.lucas@example.com", "Southern Zone"},
            {"NCT-ICT-015-02", "NCTSER-015-02", "Printer - 15-02", "PERSONAL", "ACTIVE", "PENDING", "B17", "Printer", "david.lucas@example.com", "Eastern Zone"},
            {"NCT-ICT-015-03", "NCTSER-015-03", "Scanner - 15-03", "OFFICE", "ACTIVE", "VERIFIED", "18", "Scanner", "david.lucas@example.com", "Central Zone"},
            {"NCT-ICT-015-04", "NCTSER-015-04", "Monitor - 15-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-9", "Monitor", "david.lucas@example.com", "Northern Zone"},
            {"NCT-ICT-015-05", "NCTSER-015-05", "Network Switch - 15-05", "OFFICE", "ACTIVE", "VERIFIED", "T5", "Network Switch", "david.lucas@example.com", "Zanzibar Zone"},
            {"NCT-ICT-016-01", "NCTSER-016-01", "Printer - 16-01", "OFFICE", "ACTIVE", "PENDING", "A17", "Printer", "fatuma.rashid@example.com", "Eastern Zone"},
            {"NCT-ICT-016-02", "NCTSER-016-02", "Scanner - 16-02", "PERSONAL", "ACTIVE", "VERIFIED", "B18", "Scanner", "fatuma.rashid@example.com", "Central Zone"},
            {"NCT-ICT-016-03", "NCTSER-016-03", "Monitor - 16-03", "OFFICE", "ACTIVE", "VERIFIED", "19", "Monitor", "fatuma.rashid@example.com", "Northern Zone"},
            {"NCT-ICT-016-04", "NCTSER-016-04", "Network Switch - 16-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-10", "Network Switch", "fatuma.rashid@example.com", "Zanzibar Zone"},
            {"NCT-ICT-016-05", "NCTSER-016-05", "Wireless Access Point - 16-05", "OFFICE", "DEFECTIVE", "PENDING", "U6", "Wireless Access Point", "fatuma.rashid@example.com", "Western Zone"},
            {"NCT-ICT-017-01", "NCTSER-017-01", "Scanner - 17-01", "OFFICE", "ACTIVE", "VERIFIED", "A18", "Scanner", "victor.leonard@example.com", "Central Zone"},
            {"NCT-ICT-017-02", "NCTSER-017-02", "Monitor - 17-02", "PERSONAL", "ACTIVE", "VERIFIED", "B19", "Monitor", "victor.leonard@example.com", "Northern Zone"},
            {"NCT-ICT-017-03", "NCTSER-017-03", "Network Switch - 17-03", "OFFICE", "ACTIVE", "VERIFIED", "20", "Network Switch", "victor.leonard@example.com", "Zanzibar Zone"},
            {"NCT-ICT-017-04", "NCTSER-017-04", "Wireless Access Point - 17-04", "PERSONAL", "DEFECTIVE", "PENDING", "ICT-1", "Wireless Access Point", "victor.leonard@example.com", "Western Zone"},
            {"NCT-ICT-017-05", "NCTSER-017-05", "UPS - 17-05", "OFFICE", "ACTIVE", "VERIFIED", "V7", "UPS", "victor.leonard@example.com", "Lake Zone"},
            {"NCT-ICT-018-01", "NCTSER-018-01", "Monitor - 18-01", "OFFICE", "ACTIVE", "VERIFIED", "A19", "Monitor", "agnes.robert@example.com", "Northern Zone"},
            {"NCT-ICT-018-02", "NCTSER-018-02", "Network Switch - 18-02", "PERSONAL", "ACTIVE", "VERIFIED", "B20", "Network Switch", "agnes.robert@example.com", "Zanzibar Zone"},
            {"NCT-ICT-018-03", "NCTSER-018-03", "Wireless Access Point - 18-03", "OFFICE", "DEFECTIVE", "PENDING", "21", "Wireless Access Point", "agnes.robert@example.com", "Western Zone"},
            {"NCT-ICT-018-04", "NCTSER-018-04", "UPS - 18-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-2", "UPS", "agnes.robert@example.com", "Lake Zone"},
            {"NCT-ICT-018-05", "NCTSER-018-05", "Server - 18-05", "OFFICE", "ACTIVE", "VERIFIED", "W8", "Server", "agnes.robert@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-019-01", "NCTSER-019-01", "Network Switch - 19-01", "OFFICE", "ACTIVE", "VERIFIED", "A20", "Network Switch", "mohamed.hamisi@example.com", "Zanzibar Zone"},
            {"NCT-ICT-019-02", "NCTSER-019-02", "Wireless Access Point - 19-02", "PERSONAL", "DEFECTIVE", "PENDING", "B1", "Wireless Access Point", "mohamed.hamisi@example.com", "Western Zone"},
            {"NCT-ICT-019-03", "NCTSER-019-03", "UPS - 19-03", "OFFICE", "ACTIVE", "VERIFIED", "22", "UPS", "mohamed.hamisi@example.com", "Lake Zone"},
            {"NCT-ICT-019-04", "NCTSER-019-04", "Server - 19-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-3", "Server", "mohamed.hamisi@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-019-05", "NCTSER-019-05", "Laptop - 19-05", "OFFICE", "ACTIVE", "VERIFIED", "X9", "Laptop", "mohamed.hamisi@example.com", "Southern Zone"},
            {"NCT-ICT-020-01", "NCTSER-020-01", "Wireless Access Point - 20-01", "OFFICE", "DEFECTIVE", "PENDING", "A21", "Wireless Access Point", "lucy.francis@example.com", "Western Zone"},
            {"NCT-ICT-020-02", "NCTSER-020-02", "UPS - 20-02", "PERSONAL", "ACTIVE", "VERIFIED", "B2", "UPS", "lucy.francis@example.com", "Lake Zone"},
            {"NCT-ICT-020-03", "NCTSER-020-03", "Server - 20-03", "OFFICE", "ACTIVE", "VERIFIED", "23", "Server", "lucy.francis@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-020-04", "NCTSER-020-04", "Laptop - 20-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-4", "Laptop", "lucy.francis@example.com", "Southern Zone"},
            {"NCT-ICT-020-05", "NCTSER-020-05", "Desktop Computer - 20-05", "OFFICE", "ACTIVE", "PENDING", "Y10", "Desktop Computer", "lucy.francis@example.com", "Eastern Zone"},
            {"NCT-ICT-021-01", "NCTSER-021-01", "UPS - 21-01", "OFFICE", "ACTIVE", "VERIFIED", "A22", "UPS", "peter.charles@example.com", "Lake Zone"},
            {"NCT-ICT-021-02", "NCTSER-021-02", "Server - 21-02", "PERSONAL", "ACTIVE", "VERIFIED", "B3", "Server", "peter.charles@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-021-03", "NCTSER-021-03", "Laptop - 21-03", "OFFICE", "ACTIVE", "VERIFIED", "24", "Laptop", "peter.charles@example.com", "Southern Zone"},
            {"NCT-ICT-021-04", "NCTSER-021-04", "Desktop Computer - 21-04", "PERSONAL", "ACTIVE", "PENDING", "ICT-5", "Desktop Computer", "peter.charles@example.com", "Eastern Zone"},
            {"NCT-ICT-021-05", "NCTSER-021-05", "Projector - 21-05", "OFFICE", "ACTIVE", "VERIFIED", "Z11", "Projector", "peter.charles@example.com", "Central Zone"},
            {"NCT-ICT-022-01", "NCTSER-022-01", "Server - 22-01", "OFFICE", "ACTIVE", "VERIFIED", "A23", "Server", "halima.yusuf@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-022-02", "NCTSER-022-02", "Laptop - 22-02", "PERSONAL", "ACTIVE", "VERIFIED", "B4", "Laptop", "halima.yusuf@example.com", "Southern Zone"},
            {"NCT-ICT-022-03", "NCTSER-022-03", "Desktop Computer - 22-03", "OFFICE", "ACTIVE", "PENDING", "25", "Desktop Computer", "halima.yusuf@example.com", "Eastern Zone"},
            {"NCT-ICT-022-04", "NCTSER-022-04", "Projector - 22-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-6", "Projector", "halima.yusuf@example.com", "Central Zone"},
            {"NCT-ICT-022-05", "NCTSER-022-05", "Photocopier - 22-05", "OFFICE", "ACTIVE", "VERIFIED", "A12", "Photocopier", "halima.yusuf@example.com", "Northern Zone"},
            {"NCT-ICT-023-01", "NCTSER-023-01", "Laptop - 23-01", "OFFICE", "ACTIVE", "VERIFIED", "A24", "Laptop", "robert.martin@example.com", "Southern Zone"},
            {"NCT-ICT-023-02", "NCTSER-023-02", "Desktop Computer - 23-02", "PERSONAL", "ACTIVE", "PENDING", "B5", "Desktop Computer", "robert.martin@example.com", "Eastern Zone"},
            {"NCT-ICT-023-03", "NCTSER-023-03", "Projector - 23-03", "OFFICE", "ACTIVE", "VERIFIED", "26", "Projector", "robert.martin@example.com", "Central Zone"},
            {"NCT-ICT-023-04", "NCTSER-023-04", "Photocopier - 23-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-7", "Photocopier", "robert.martin@example.com", "Northern Zone"},
            {"NCT-ICT-023-05", "NCTSER-023-05", "Printer - 23-05", "OFFICE", "ACTIVE", "VERIFIED", "B13", "Printer", "robert.martin@example.com", "Zanzibar Zone"},
            {"NCT-ICT-024-01", "NCTSER-024-01", "Desktop Computer - 24-01", "OFFICE", "ACTIVE", "PENDING", "A25", "Desktop Computer", "zainab.ali@example.com", "Eastern Zone"},
            {"NCT-ICT-024-02", "NCTSER-024-02", "Projector - 24-02", "PERSONAL", "ACTIVE", "VERIFIED", "B6", "Projector", "zainab.ali@example.com", "Central Zone"},
            {"NCT-ICT-024-03", "NCTSER-024-03", "Photocopier - 24-03", "OFFICE", "ACTIVE", "VERIFIED", "27", "Photocopier", "zainab.ali@example.com", "Northern Zone"},
            {"NCT-ICT-024-04", "NCTSER-024-04", "Printer - 24-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-8", "Printer", "zainab.ali@example.com", "Zanzibar Zone"},
            {"NCT-ICT-024-05", "NCTSER-024-05", "Scanner - 24-05", "OFFICE", "ACTIVE", "PENDING", "C14", "Scanner", "zainab.ali@example.com", "Western Zone"},
            {"NCT-ICT-025-01", "NCTSER-025-01", "Projector - 25-01", "OFFICE", "ACTIVE", "VERIFIED", "A26", "Projector", "samuel.george@example.com", "Central Zone"},
            {"NCT-ICT-025-02", "NCTSER-025-02", "Photocopier - 25-02", "PERSONAL", "ACTIVE", "VERIFIED", "B7", "Photocopier", "samuel.george@example.com", "Northern Zone"},
            {"NCT-ICT-025-03", "NCTSER-025-03", "Printer - 25-03", "OFFICE", "ACTIVE", "VERIFIED", "28", "Printer", "samuel.george@example.com", "Zanzibar Zone"},
            {"NCT-ICT-025-04", "NCTSER-025-04", "Scanner - 25-04", "PERSONAL", "ACTIVE", "PENDING", "ICT-9", "Scanner", "samuel.george@example.com", "Western Zone"},
            {"NCT-ICT-025-05", "NCTSER-025-05", "Monitor - 25-05", "OFFICE", "ACTIVE", "VERIFIED", "D15", "Monitor", "samuel.george@example.com", "Lake Zone"},
            {"NCT-ICT-026-01", "NCTSER-026-01", "Photocopier - 26-01", "OFFICE", "ACTIVE", "VERIFIED", "A27", "Photocopier", "rosemary.john@example.com", "Northern Zone"},
            {"NCT-ICT-026-02", "NCTSER-026-02", "Printer - 26-02", "PERSONAL", "ACTIVE", "VERIFIED", "B8", "Printer", "rosemary.john@example.com", "Zanzibar Zone"},
            {"NCT-ICT-026-03", "NCTSER-026-03", "Scanner - 26-03", "OFFICE", "ACTIVE", "PENDING", "29", "Scanner", "rosemary.john@example.com", "Western Zone"},
            {"NCT-ICT-026-04", "NCTSER-026-04", "Monitor - 26-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-10", "Monitor", "rosemary.john@example.com", "Lake Zone"},
            {"NCT-ICT-026-05", "NCTSER-026-05", "Network Switch - 26-05", "OFFICE", "DEFECTIVE", "VERIFIED", "E1", "Network Switch", "rosemary.john@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-027-01", "NCTSER-027-01", "Printer - 27-01", "OFFICE", "ACTIVE", "VERIFIED", "A28", "Printer", "michael.thomas@example.com", "Zanzibar Zone"},
            {"NCT-ICT-027-02", "NCTSER-027-02", "Scanner - 27-02", "PERSONAL", "ACTIVE", "PENDING", "B9", "Scanner", "michael.thomas@example.com", "Western Zone"},
            {"NCT-ICT-027-03", "NCTSER-027-03", "Monitor - 27-03", "OFFICE", "ACTIVE", "VERIFIED", "30", "Monitor", "michael.thomas@example.com", "Lake Zone"},
            {"NCT-ICT-027-04", "NCTSER-027-04", "Network Switch - 27-04", "PERSONAL", "DEFECTIVE", "VERIFIED", "ICT-1", "Network Switch", "michael.thomas@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-027-05", "NCTSER-027-05", "Wireless Access Point - 27-05", "OFFICE", "ACTIVE", "VERIFIED", "F2", "Wireless Access Point", "michael.thomas@example.com", "Southern Zone"},
            {"NCT-ICT-028-01", "NCTSER-028-01", "Scanner - 28-01", "OFFICE", "ACTIVE", "PENDING", "A29", "Scanner", "asha.hamad@example.com", "Western Zone"},
            {"NCT-ICT-028-02", "NCTSER-028-02", "Monitor - 28-02", "PERSONAL", "ACTIVE", "VERIFIED", "B10", "Monitor", "asha.hamad@example.com", "Lake Zone"},
            {"NCT-ICT-028-03", "NCTSER-028-03", "Network Switch - 28-03", "OFFICE", "DEFECTIVE", "VERIFIED", "31", "Network Switch", "asha.hamad@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-028-04", "NCTSER-028-04", "Wireless Access Point - 28-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-2", "Wireless Access Point", "asha.hamad@example.com", "Southern Zone"},
            {"NCT-ICT-028-05", "NCTSER-028-05", "UPS - 28-05", "OFFICE", "ACTIVE", "PENDING", "G3", "UPS", "asha.hamad@example.com", "Eastern Zone"},
            {"NCT-ICT-029-01", "NCTSER-029-01", "Monitor - 29-01", "OFFICE", "ACTIVE", "VERIFIED", "A30", "Monitor", "franklin.james@example.com", "Lake Zone"},
            {"NCT-ICT-029-02", "NCTSER-029-02", "Network Switch - 29-02", "PERSONAL", "DEFECTIVE", "VERIFIED", "B11", "Network Switch", "franklin.james@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-029-03", "NCTSER-029-03", "Wireless Access Point - 29-03", "OFFICE", "ACTIVE", "VERIFIED", "32", "Wireless Access Point", "franklin.james@example.com", "Southern Zone"},
            {"NCT-ICT-029-04", "NCTSER-029-04", "UPS - 29-04", "PERSONAL", "ACTIVE", "PENDING", "ICT-3", "UPS", "franklin.james@example.com", "Eastern Zone"},
            {"NCT-ICT-029-05", "NCTSER-029-05", "Server - 29-05", "OFFICE", "ACTIVE", "VERIFIED", "H4", "Server", "franklin.james@example.com", "Central Zone"},
            {"NCT-ICT-030-01", "NCTSER-030-01", "Network Switch - 30-01", "OFFICE", "DEFECTIVE", "VERIFIED", "A1", "Network Switch", "janet.wilson@example.com", "Southern Highlands Zone"},
            {"NCT-ICT-030-02", "NCTSER-030-02", "Wireless Access Point - 30-02", "PERSONAL", "ACTIVE", "VERIFIED", "B12", "Wireless Access Point", "janet.wilson@example.com", "Southern Zone"},
            {"NCT-ICT-030-03", "NCTSER-030-03", "UPS - 30-03", "OFFICE", "ACTIVE", "PENDING", "33", "UPS", "janet.wilson@example.com", "Eastern Zone"},
            {"NCT-ICT-030-04", "NCTSER-030-04", "Server - 30-04", "PERSONAL", "ACTIVE", "VERIFIED", "ICT-4", "Server", "janet.wilson@example.com", "Central Zone"},
            {"NCT-ICT-030-05", "NCTSER-030-05", "Laptop - 30-05", "OFFICE", "ACTIVE", "VERIFIED", "I5", "Laptop", "janet.wilson@example.com", "Northern Zone"}
    };

    private static final String[] STAFF_EMPLOYEE_IDS = {
            "NCT-EMP-00001", "NCT-EMP-00002", "NCT-EMP-00003", "NCT-EMP-00004", "NCT-EMP-00005",
            "NCT-EMP-00006", "NCT-EMP-00007", "NCT-EMP-00008", "NCT-EMP-00009", "NCT-EMP-00010",
            "NCT-EMP-00011", "NCT-EMP-00012", "NCT-EMP-00013", "NCT-EMP-00014", "NCT-EMP-00015",
            "NCT-EMP-00016", "NCT-EMP-00017", "NCT-EMP-00018", "NCT-EMP-00019", "NCT-EMP-00020",
            "NCT-EMP-00021", "NCT-EMP-00022", "NCT-EMP-00023", "NCT-EMP-00024", "NCT-EMP-00025",
            "NCT-EMP-00026", "NCT-EMP-00027", "NCT-EMP-00028", "NCT-EMP-00029", "NCT-EMP-00030"
    };

    private static final String[] STAFF_FULL_NAMES = {
            "Amani Juma", "Neema Hassan", "Baraka Mushi", "Rehema Said", "Daniel Joseph", "Grace Peter",
            "Ibrahim Salum", "Joyce Emmanuel", "Kelvin Michael", "Sophia George", "Hassan Omari",
            "Esther William", "Patrick Simon", "Mariam Ali", "David Lucas", "Fatuma Rashid",
            "Victor Leonard", "Agnes Robert", "Mohamed Hamisi", "Lucy Francis", "Peter Charles",
            "Halima Yusuf", "Robert Martin", "Zainab Ali", "Samuel George", "Rosemary John",
            "Michael Thomas", "Asha Hamad", "Franklin James", "Janet Wilson"
    };

    private static final String[] STAFF_EMAILS = {
            "amani.juma@example.com", "neema.hassan@example.com", "baraka.mushi@example.com",
            "rehema.said@example.com", "daniel.joseph@example.com", "grace.peter@example.com",
            "ibrahim.salum@example.com", "joyce.emmanuel@example.com", "kelvin.michael@example.com",
            "sophia.george@example.com", "hassan.omari@example.com", "esther.william@example.com",
            "patrick.simon@example.com", "mariam.ali@example.com", "david.lucas@example.com",
            "fatuma.rashid@example.com", "victor.leonard@example.com", "agnes.robert@example.com",
            "mohamed.hamisi@example.com", "lucy.francis@example.com", "peter.charles@example.com",
            "halima.yusuf@example.com", "robert.martin@example.com", "zainab.ali@example.com",
            "samuel.george@example.com", "rosemary.john@example.com", "michael.thomas@example.com",
            "asha.hamad@example.com", "franklin.james@example.com", "janet.wilson@example.com"
    };

    private static final String[] STAFF_PHONES = {
            "0712345001", "0712345002", "0712345003", "0712345004", "0712345005", "0712345006",
            "0712345007", "0712345008", "0712345009", "0712345010", "0712345011", "0712345012",
            "0712345013", "0712345014", "0712345015", "0712345016", "0712345017", "0712345018",
            "0712345019", "0712345020", "0712345021", "0712345022", "0712345023", "0712345024",
            "0712345025", "0712345026", "0712345027", "0712345028", "0712345029", "0712345030"
    };

    private static final String[] STAFF_ORG = {
            "Registration and Accreditation Section",
            "Labour Market Analysis and Curriculum Development Section", "Admission Section",
            "Examinations and Certification Section", "Academic Quality Audit Section",
            "Compliance and Enforcement Section", "Human Resource Management and Administration Section",
            "Planning, Monitoring and Evaluation Section", "Registration and Accreditation Section",
            "Labour Market Analysis and Curriculum Development Section", "Admission Section",
            "Examinations and Certification Section", "Academic Quality Audit Section",
            "Compliance and Enforcement Section", "Human Resource Management and Administration Section",
            "Planning, Monitoring and Evaluation Section", "Registration and Accreditation Section",
            "Labour Market Analysis and Curriculum Development Section", "Admission Section",
            "Examinations and Certification Section", "Academic Quality Audit Section",
            "Compliance and Enforcement Section", "Human Resource Management and Administration Section",
            "Planning, Monitoring and Evaluation Section",
            "Information and Communication Technology Unit", "Finance and Accounts Unit",
            "Procurement Management Unit", "Internal Audit Unit", "Communications and Marketing Unit",
            "Legal Services Unit"
    };

    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final SectionRepository sectionRepository;
    private final UnitRepository unitRepository;
    private final ZoneRepository zoneRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final String[] ASSET_DEVICE_TYPES = {
            "Laptop", "Desktop Computer", "Projector", "Photocopier", "Printer",
            "Scanner", "Monitor", "Network Switch", "Wireless Access Point",
            "UPS", "Server"
    };

    public DataSeedInitializer(UserRepository userRepository,
                               AssetRepository assetRepository,
                               DeviceTypeRepository deviceTypeRepository,
                               SectionRepository sectionRepository,
                               UnitRepository unitRepository,
                               ZoneRepository zoneRepository,
                               JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
        this.deviceTypeRepository = deviceTypeRepository;
        this.sectionRepository = sectionRepository;
        this.unitRepository = unitRepository;
        this.zoneRepository = zoneRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByRole(Role.STAFF)) {
            log.info("[DataSeedInitializer] Staff users already seeded, skipping.");
            return;
        }

        List<DeviceType> deviceTypes = seedDeviceTypes();
        List<Zone> zones = zonesByName();
        List<Section> sections = sectionsByName();
        List<Unit> units = unitsByName();

        if (sections.size() != SECTIONS.length || units.size() != UNITS.length
                || zones.size() != ZONES.length || !allDeviceTypesPresent(deviceTypes)) {
            log.info("[DataSeedInitializer] Master data not fully in place, skipping seed "
                    + "(sections={}, units={}, zones={}, device-types={}).",
                    sections.size(), units.size(), zones.size(), deviceTypes.size());
            return;
        }

        Map<String, Section> sectionByEmail = new java.util.HashMap<>();
        Map<String, Unit> unitByEmail = new java.util.HashMap<>();
        for (int i = 0; i < STAFF_EMAILS.length; i++) {
            String email = STAFF_EMAILS[i];
            if (isUnit(i)) {
                unitByEmail.put(email, findByUnit(units, STAFF_ORG[i]));
            } else {
                sectionByEmail.put(email, findBySection(sections, STAFF_ORG[i]));
            }
        }

        List<User> staff = seedStaff(sectionByEmail, unitByEmail);
        seedAssets(staff, deviceTypes, zones);
        advanceEmployeeIdSequence();

        log.info("[DataSeedInitializer] Seeded {} staff users and {} assets.",
                staff.size(), assetRepository.count());
    }

    private List<User> seedStaff(Map<String, Section> sectionByEmail,
                                 Map<String, Unit> unitByEmail) {
        List<User> staff = new ArrayList<>();
        for (int i = 0; i < STAFF_EMAILS.length; i++) {
            String email = STAFF_EMAILS[i];
            User user = new User();
            user.setEmployeeId(STAFF_EMPLOYEE_IDS[i]);
            user.setFullName(STAFF_FULL_NAMES[i]);
            user.setUsername(email);
            user.setEmail(email);
            user.setPassword(STAFF_PASSWORD_HASH);
            user.setPhoneNumber(STAFF_PHONES[i]);
            user.setRole(Role.STAFF);
            user.setEnabled(true);
            user.setSetupCompleted(true);

            if (isUnit(i)) {
                user.setUnit(unitByEmail.get(email));
            } else {
                Section section = sectionByEmail.get(email);
                user.setSection(section);
                user.setDirectorate(section.getDirectorate());
            }
            userRepository.save(user);
            staff.add(user);
        }
        return staff;
    }

    private void seedAssets(List<User> staff, List<DeviceType> deviceTypes, List<Zone> zones) {
        Map<String, User> userByEmail = new java.util.HashMap<>();
        for (User u : staff) {
            userByEmail.put(u.getEmail(), u);
        }
        Map<String, DeviceType> typeByName = new java.util.HashMap<>();
        for (DeviceType dt : deviceTypes) {
            typeByName.put(dt.getName(), dt);
        }
        Map<String, Zone> zoneByName = new java.util.HashMap<>();
        for (Zone z : zones) {
            zoneByName.put(z.getName(), z);
        }

        for (String[] row : ASSETS) {
            Asset asset = new Asset();
            asset.setAssetNumber(row[0]);
            asset.setSerialNumber(row[1]);
            asset.setDeviceName(row[2]);
            asset.setOwnershipType(OwnershipType.valueOf(row[3]));
            asset.setDeviceStatus(DeviceStatus.valueOf(row[4]));
            asset.setVerificationStatus(VerificationStatus.valueOf(row[5]));
            asset.setOffice(row[6]);
            asset.setDeviceType(typeByName.get(row[7]));
            asset.setUser(userByEmail.get(row[8]));
            asset.setZone(zoneByName.get(row[9]));
            assetRepository.save(asset);
        }
    }

    private List<DeviceType> seedDeviceTypes() {
        List<DeviceType> created = new ArrayList<>();
        Map<String, String> desc = new java.util.HashMap<>();
        desc.put("Laptop", "Portable computer for staff use");
        desc.put("Desktop Computer", "Fixed workstation computer");
        desc.put("Projector", "Presentation projector");
        desc.put("Photocopier", "Multi-function document copier");
        desc.put("Printer", "Document printer");
        desc.put("Scanner", "Document scanner");
        desc.put("Monitor", "Standalone computer display");
        desc.put("Network Switch", "Ethernet network switch");
        desc.put("Wireless Access Point", "Wi-Fi access point");
        desc.put("UPS", "Uninterruptible power supply");
        desc.put("Server", "Network/server machine");

        if (deviceTypeRepository.count() == 0) {
            for (Map.Entry<String, String> e : desc.entrySet()) {
                DeviceType dt = new DeviceType();
                dt.setName(e.getKey());
                dt.setDescription(e.getValue());
                deviceTypeRepository.save(dt);
            }
        }
        return deviceTypeRepository.findAll();
    }

    private boolean allDeviceTypesPresent(List<DeviceType> deviceTypes) {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (DeviceType dt : deviceTypes) {
            names.add(dt.getName());
        }
        for (String name : ASSET_DEVICE_TYPES) {
            if (!names.contains(name)) {
                return false;
            }
        }
        return true;
    }

    private List<Section> sectionsByName() {
        List<Section> result = new ArrayList<>();
        for (String name : SECTIONS) {
            sectionRepository.findByName(name).ifPresent(result::add);
        }
        return result;
    }

    private List<Unit> unitsByName() {
        List<Unit> result = new ArrayList<>();
        for (String name : UNITS) {
            unitRepository.findByName(name).ifPresent(result::add);
        }
        return result;
    }

    private List<Zone> zonesByName() {
        List<Zone> result = new ArrayList<>();
        for (String name : ZONES) {
            zoneRepository.findByName(name).ifPresent(result::add);
        }
        return result;
    }

    private static Section findBySection(List<Section> sections, String name) {
        for (Section s : sections) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        throw new IllegalStateException("Missing master section: " + name);
    }

    private static Unit findByUnit(List<Unit> units, String name) {
        for (Unit u : units) {
            if (u.getName().equals(name)) {
                return u;
            }
        }
        throw new IllegalStateException("Missing master unit: " + name);
    }

    private static boolean isUnit(int i) {
        return i >= 24;
    }

    private void advanceEmployeeIdSequence() {
        try {
            jdbcTemplate.queryForObject(
                    "SELECT setval('employee_id_seq', 30, true)", Long.class);
        } catch (Exception e) {
            log.warn("[DataSeedInitializer] Could not advance employee_id_seq: {}", e.getMessage());
        }
    }
}
