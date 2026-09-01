package tz.go.nactvet.ict_inventory_management;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
class StaffSelfRegistrationFlowTest {

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

    private String registerBody(String email, String password, boolean matchingConfirm) {
        String confirm = matchingConfirm ? password : "different-password";
        return "{"
                + "\"fullName\":\"John Doe\","
                + "\"email\":\"" + email + "\","
                + "\"phoneNumber\":\"0712345678\","
                + "\"password\":\"" + password + "\","
                + "\"confirmPassword\":\"" + confirm + "\","
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
    void registerReturnsSafeResponseWithEmployeeIdRoleAndStatus() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("john.doe@nactvet.go.tz", "secret123", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.employeeId", startsWith("NCT-EMP-")))
                .andExpect(jsonPath("$.email").value("john.doe@nactvet.go.tz"))
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void registerThreeStaffResultsInUniqueEmployeeIds() throws Exception {
        String[] emails = {
                "john.doe@nactvet.go.tz",
                "jane.smith@nactvet.go.tz",
                "peter.pan@nactvet.go.tz"
        };
        String[] employeeIds = new String[emails.length];
        for (int i = 0; i < emails.length; i++) {
            MvcResult result = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerBody(emails[i], "secret123", true)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.employeeId", startsWith("NCT-EMP-")))
                    .andReturn();
            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            employeeIds[i] = node.get("employeeId").asText();
        }
        if (employeeIds[0].equals(employeeIds[1]) || employeeIds[1].equals(employeeIds[2])
                || employeeIds[0].equals(employeeIds[2])) {
            throw new AssertionError("Employee IDs must be unique but got: "
                    + String.join(", ", employeeIds));
        }
    }

    @Test
    void duplicateEmailRegistrationFails() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("john.doe@nactvet.go.tz", "secret123", true)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("john.doe@nactvet.go.tz", "secret123", true)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Email address is already registered")));
    }

    @Test
    void mismatchedConfirmPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("john.doe@nactvet.go.tz", "secret123", false)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginSucceedsUsingEmailAddress() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("john.doe@nactvet.go.tz", "secret123", true)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("john.doe@nactvet.go.tz", "secret123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("john.doe@nactvet.go.tz"));
    }

    @Test
    void deactivatedStaffCannotLoginButCanAfterActivation() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("john.doe@nactvet.go.tz", "secret123", true)))
                .andExpect(status().isCreated());

        long staffId = findStaffIdByEmail("john.doe@nactvet.go.tz");
        String adminToken = loginAndGetToken("admin@nactvet.go.tz", "admin123");

        mockMvc.perform(post("/admin/staff/" + staffId + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("john.doe@nactvet.go.tz", "secret123")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/admin/staff/" + staffId + "/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("john.doe@nactvet.go.tz", "secret123")))
                .andExpect(status().isOk());
    }

    @Test
    void assetOwnershipUsesAuthenticatedUserNotSuppliedId() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("john.doe@nactvet.go.tz", "secret123", true)))
                .andExpect(status().isCreated());

        String token = loginAndGetToken("john.doe@nactvet.go.tz", "secret123");

        String assetBody = "{"
                + "\"serialNumber\":\"SN-001\","
                + "\"deviceName\":\"ICT Laptop\","
                + "\"deviceTypeId\":" + deviceTypeId + ","
                + "\"ownershipType\":\"OFFICE\","
                + "\"deviceStatus\":\"ACTIVE\","
                + "\"zoneId\":" + zoneId + ","
                + "\"office\":\"B12\""
                + "}";

        MvcResult result = mockMvc.perform(post("/assets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assetBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        Long assetUserId = node.get("userId").asLong();
        Long registeredUserId = findUserIdByEmail("john.doe@nactvet.go.tz");
        if (!assetUserId.equals(registeredUserId)) {
            throw new AssertionError("Asset must be owned by the authenticated staff user. Expected "
                    + registeredUserId + " but got " + assetUserId);
        }
    }

    private long findUserIdByEmail(String email) {
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, email);
        return id;
    }

    private long findStaffIdByEmail(String email) {
        return findUserIdByEmail(email);
    }
}
