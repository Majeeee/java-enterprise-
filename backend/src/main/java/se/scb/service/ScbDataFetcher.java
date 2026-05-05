package se.scb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.scb.model.Migration;
import se.scb.repository.MigrationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ScbDataFetcher {

    private static final Logger log = LoggerFactory.getLogger(ScbDataFetcher.class);

    private static final Map<String, String> AGE_GROUPS = Map.ofEntries(
            Map.entry("0-9", ageRange(0, 9)),
            Map.entry("10-19", ageRange(10, 19)),
            Map.entry("20-29", ageRange(20, 29)),
            Map.entry("30-39", ageRange(30, 39)),
            Map.entry("40-49", ageRange(40, 49)),
            Map.entry("50-59", ageRange(50, 59)),
            Map.entry("60-69", ageRange(60, 69)),
            Map.entry("70-79", ageRange(70, 79)),
            Map.entry("80-89", ageRange(80, 89)),
            Map.entry("90-99", ageRange(90, 99)),
            Map.entry("100+", "\"100+\"")
    );

    private final ScbApiClient apiClient;
    private final ScbResponseParser parser;
    private final MigrationRepository migrationRepository;

    public ScbDataFetcher(ScbApiClient apiClient,
                          ScbResponseParser parser,
                          MigrationRepository migrationRepository) {
        this.apiClient = apiClient;
        this.parser = parser;
        this.migrationRepository = migrationRepository;
    }

    /**
     * Fetches real migration data from SCB and persists it.
     * Falls back to a warning log if the API is unreachable,
     * so the application still starts in offline/test environments.
     */
    public void fetchAndPersist() {
        log.info("Hamtar verklig SCB-data...");
        try {
            List<Migration> migrations = new ArrayList<>();
            for (Map.Entry<String, String> ageGroup : AGE_GROUPS.entrySet()) {
                String json = apiClient.fetchMigrationData(ageGroup.getValue());
                migrations.addAll(parser.parse(json, ageGroup.getKey()));
            }

            if (migrations.isEmpty()) {
                log.warn("SCB API returnerade ingen parsbar data");
                return;
            }

            migrationRepository.saveAll(migrations);
            log.info("Sparade {} rader med verklig SCB-data", migrations.size());

        } catch (Exception e) {
            log.error("Kunde inte hamta data fran SCB API: {}. Databasen forblir tom.", e.getMessage());
        }
    }

    private static String ageRange(int startInclusive, int endInclusive) {
        List<String> ages = new ArrayList<>();
        for (int age = startInclusive; age <= endInclusive; age++) {
            ages.add("\"" + age + "\"");
        }
        return String.join(",", ages);
    }
}
