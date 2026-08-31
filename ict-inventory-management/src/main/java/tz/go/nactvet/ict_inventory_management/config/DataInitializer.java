package tz.go.nactvet.ict_inventory_management.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.enums.Role;
import tz.go.nactvet.ict_inventory_management.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.email:admin@nactvet.go.tz}")
    private String adminEmail;

    @Value("${app.admin.employee-id:EMP001}")
    private String adminEmployeeId;

    @Value("${app.admin.full-name:System Administrator}")
    private String adminFullName;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = new User();
            admin.setEmployeeId(adminEmployeeId);
            admin.setFullName(adminFullName);
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);
            log.info("Initial ADMIN account created with username: {}", adminUsername);
            log.warn("IMPORTANT: Change the default admin password before production deployment!");
        } else {
            log.info("ADMIN account already exists, skipping initial admin creation");
        }
    }
}
