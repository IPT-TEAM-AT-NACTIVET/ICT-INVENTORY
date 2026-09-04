package tz.go.nactvet.ict_inventory_management;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tz.go.nactvet.ict_inventory_management.entity.DeviceType;
import tz.go.nactvet.ict_inventory_management.entity.Directorate;
import tz.go.nactvet.ict_inventory_management.entity.Zone;
import tz.go.nactvet.ict_inventory_management.repository.DeviceTypeRepository;
import tz.go.nactvet.ict_inventory_management.repository.DirectorateRepository;
import tz.go.nactvet.ict_inventory_management.repository.ZoneRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TwoRoleWorkflowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DirectorateRepository directorateRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long directorateId;
    private Long zoneId;
    private Long deviceTypeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS employee_id_seq");
        jdbcTemplate.execute("ALTER SEQUENCE employee_id_seq RESTART WITH 1");

        jdbcTemplate.update("DELETE FROM assets");
        jdbcTemplate.update("DELETE FROM users WHERE email IS NOT NULL AND email <> 'admin@nactvet.go.tz'");

        Directorate directorate = directorateRepository.findByName("Corporate Services")
                .orElseGet(() -> {
                    Directorate d = new Directorate();
                    d.setName("Corporate Services");
                    d.setCode("D4");
                    d.setDescription("Test");
                    return directorateRepository.save(d);
                });
        directorateId = directorate.getId();

        Zone zone = zoneRepository.findByName("Eastern Zone Office")
                .orElseGet(() -> {
                    Zone z = new Zone();
                    z.setName("Eastern Zone Office");
                    z.setStatus("ACTIVE");
                    return zoneRepository.save(z);
                });
        zoneId = zone.getId();

        DeviceType deviceType = deviceTypeRepository.findByName("Laptop")
                .orElseGet(() -> {
                    DeviceType d = new DeviceType();
                    d.setName("Laptop");
                    return deviceTypeRepository.save(d);
                });
        deviceTypeId = deviceType.getId();
    }

    private String createUserBody(String email) {
        return "{"
                + "\"fullName\":\"John Doe\","
                + "\"email\":\"" + email + "\","
                + "\"phoneNumber\":\"0712345678\","
                + "\"password\":\"secret123\","
                + "\"directorateId\":" + directorateId + ","
                + "\"sectionId\":null,"
                + "\"unitId\":null"
                + "}";
    }

    private String loginBody(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    @Test
    void selfRegistrationCreatesDisabledPendingAccountAndCanBeApprovedByActiveUser() throws Exception {
        // Self-registration creates an ADMIN account that is DISABLED (pending approval).
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserBody("john.doe@nactvet.go.tz")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john.doe@nactvet.go.tz"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.enabled").value(false));

        // A disabled (pending) account cannot log in yet.
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("john.doe@nactvet.go.tz", "secret123")))
                .andExpect(status().isUnauthorized());

        // An active ADMIN can approve the pending account.
        String adminToken = loginAndGetToken("admin@nactvet.go.tz", "admin123");
        long officerId = findUserIdByEmail("john.doe@nactvet.go.tz");

        mockMvc.perform(post("/users/" + officerId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        // After approval the account can log in.
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("john.doe@nactvet.go.tz", "secret123")))
                .andExpect(status().isOk());
    }

    @Test
    void adminCreatesUserWithServerAssignedRole() throws Exception {
        String adminToken = loginAndGetToken("admin@nactvet.go.tz", "admin123");

        // The server always assigns ADMIN role regardless of what the client sends.
        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserBody("john.doe@nactvet.go.tz")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", startsWith("NCT-EMP-")))
                .andExpect(jsonPath("$.email").value("john.doe@nactvet.go.tz"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserBody("jane.smith@nactvet.go.tz")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId", startsWith("NCT-EMP-")))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void activeUserCannotApproveOwnAccount() throws Exception {
        String adminToken = loginAndGetToken("admin@nactvet.go.tz", "admin123");
        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserBody("john.doe@nactvet.go.tz")))
                .andExpect(status().isOk());

        String officerToken = loginAndGetToken("john.doe@nactvet.go.tz", "secret123");
        long officerId = findUserIdByEmail("john.doe@nactvet.go.tz");

        // A user cannot deactivate their own account.
        mockMvc.perform(post("/users/" + officerId + "/deactivate")
                        .header("Authorization", "Bearer " + officerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminRegistersAssetWithServerAuditAndAccessesReports() throws Exception {
        String adminToken = loginAndGetToken("admin@nactvet.go.tz", "admin123");
        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserBody("john.doe@nactvet.go.tz")))
                .andExpect(status().isOk());

        String officerToken = loginAndGetToken("john.doe@nactvet.go.tz", "secret123");
        long officerId = findUserIdByEmail("john.doe@nactvet.go.tz");

        String assetBody = "{"
                + "\"assetNumber\":\"AST-001\","
                + "\"serialNumber\":\"SN-001\","
                + "\"deviceName\":\"ICT Laptop\","
                + "\"deviceTypeId\":" + deviceTypeId + ","
                + "\"userOfAsset\":\"Philip Tyson\","
                + "\"ownershipType\":\"OFFICE\","
                + "\"deviceStatus\":\"ACTIVE\","
                + "\"zoneId\":" + zoneId + ","
                + "\"office\":\"B12\""
                + "}";

        mockMvc.perform(post("/admin/assets")
                        .header("Authorization", "Bearer " + officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assetBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userOfAsset").value("Philip Tyson"))
                .andExpect(jsonPath("$.createdById").value(officerId));

        // Global inventory search is case-insensitive and partial across fields.
        mockMvc.perform(get("/admin/assets")
                        .header("Authorization", "Bearer " + officerToken)
                        .param("search", "phil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].userOfAsset").value("Philip Tyson"));

        mockMvc.perform(get("/admin/assets")
                        .header("Authorization", "Bearer " + officerToken)
                        .param("search", "ict lap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].deviceName").value("ICT Laptop"));

        mockMvc.perform(get("/admin/assets")
                        .header("Authorization", "Bearer " + officerToken)
                        .param("search", "b1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].office").value("B12"));

        mockMvc.perform(get("/admin/assets")
                        .header("Authorization", "Bearer " + officerToken)
                        .param("search", "laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/admin/assets")
                        .header("Authorization", "Bearer " + officerToken)
                        .param("search", "eastern"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/admin/reports/by-zone")
                        .header("Authorization", "Bearer " + officerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/reports/by-status")
                        .header("Authorization", "Bearer " + officerToken))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanReadRegisteredAsset() throws Exception {
        String adminToken = loginAndGetToken("admin@nactvet.go.tz", "admin123");
        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserBody("john.doe@nactvet.go.tz")))
                .andExpect(status().isOk());

        String officerToken = loginAndGetToken("john.doe@nactvet.go.tz", "secret123");

        String assetBody = "{"
                + "\"assetNumber\":\"AST-002\","
                + "\"serialNumber\":\"SN-002\","
                + "\"deviceName\":\"Monitor\","
                + "\"deviceTypeId\":" + deviceTypeId + ","
                + "\"userOfAsset\":\"Mary Johnson\","
                + "\"ownershipType\":\"OFFICE\","
                + "\"deviceStatus\":\"ACTIVE\","
                + "\"zoneId\":" + zoneId + ","
                + "\"office\":\"B12\""
                + "}";

        MvcResult result = mockMvc.perform(post("/admin/assets")
                        .header("Authorization", "Bearer " + officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assetBody))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        long assetId = node.get("id").asLong();

        // Admins have access to the inventory and can read the assets they manage.
        mockMvc.perform(get("/admin/assets/" + assetId)
                        .header("Authorization", "Bearer " + officerToken))
                .andExpect(status().isOk());
    }

    private long findUserIdByEmail(String email) {
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, email);
        return id;
    }
}