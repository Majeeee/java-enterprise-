package se.scb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import se.scb.model.Migration;
import se.scb.model.Role;
import se.scb.model.User;
import se.scb.repository.MigrationRepository;
import se.scb.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final MigrationRepository migrationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(MigrationRepository migrationRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.migrationRepository = migrationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createDefaultAdminIfMissing();
        loadSampleMigrationDataIfEmpty();
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

    private void loadSampleMigrationDataIfEmpty() {
        if (migrationRepository.count() > 0) {
            log.info("Migreringsdata finns redan i databasen, hoppar över inläsning");
            return;
        }

        log.info("Laddar in exempeldata för SCB-flyttningar...");

        List<Migration> migrations = new ArrayList<>();

        String[] regions = {
                "Stockholms län", "Västra Götalands län", "Skåne län",
                "Östergötlands län", "Örebro län", "Uppsala län"
        };
        String[] codes = {"01", "14", "12", "05", "18", "03"};
        String[] genders = {"men", "women"};
        String[] ageGroups = {"0-4", "5-9", "10-14", "15-19", "20-24",
                "25-29", "30-34", "35-39", "40-44", "45-49",
                "50-54", "55-59", "60-64", "65-69", "70+"};

        for (int i = 0; i < regions.length; i++) {
            for (String gender : genders) {
                for (String ageGroup : ageGroups) {
                    for (int year = 1997; year <= 2024; year++) {
                        int baseIn = 500 + (int)(Math.random() * 2000);
                        int baseOut = 400 + (int)(Math.random() * 1800);
                        migrations.add(new Migration(
                                regions[i], codes[i], gender, ageGroup,
                                year, baseIn, baseOut
                        ));
                    }
                }
            }
        }

        migrationRepository.saveAll(migrations);
        log.info("Laddade {} migreringsrader", migrations.size());
    }
}
