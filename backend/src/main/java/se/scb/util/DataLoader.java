package se.scb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import se.scb.model.Role;
import se.scb.model.User;
import se.scb.repository.MigrationRepository;
import se.scb.repository.UserRepository;
import se.scb.service.ScbDataFetcher;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final MigrationRepository migrationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ScbDataFetcher scbDataFetcher;

    public DataLoader(MigrationRepository migrationRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      ScbDataFetcher scbDataFetcher) {
        this.migrationRepository = migrationRepository;
        this.userRepository      = userRepository;
        this.passwordEncoder     = passwordEncoder;
        this.scbDataFetcher      = scbDataFetcher;
    }

    @Override
    public void run(String... args) {
        createDefaultAdminIfMissing();
        loadRealMigrationDataIfEmpty();
    }

    private void createDefaultAdminIfMissing() {
        if (!userRepository.existsByEmail("admin@scb.se")) {
            User admin = new User(
                    "admin@scb.se",
                    passwordEncoder.encode("Admin123!"),
                    "Admin",
                    "SCB"
            );
            admin.setRole(Role.ROLE_ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Standard-admin skapad: admin@scb.se / Admin123!");
        }
    }

    private void loadRealMigrationDataIfEmpty() {
        if (migrationRepository.count() > 0) {
            log.info("Migreringsdata finns redan i databasen, hoppar över inläsning");
            return;
        }
        scbDataFetcher.fetchAndPersist();
    }
}
