package se.scb.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import se.scb.dto.MigrationResponse;
import se.scb.model.Migration;
import se.scb.repository.MigrationRepository;
import se.scb.repository.MigrationSpecification;

import java.util.Comparator;
import java.util.List;

@Service
public class MigrationService {

    private final MigrationRepository migrationRepository;

    public MigrationService(MigrationRepository migrationRepository) {
        this.migrationRepository = migrationRepository;
    }

    public List<MigrationResponse> findWithFilters(String region, String gender,
                                                    String ageGroup, Integer year) {
        Specification<Migration> spec = MigrationSpecification
                .withFilters(region, gender, ageGroup, year);

        return migrationRepository.findAll(spec)
                .stream()
                .map(MigrationResponse::fromEntity)
                .toList();
    }

    public List<String> getAllRegions() {
        return migrationRepository.findDistinctRegions();
    }

    public List<String> getAllAgeGroups() {
        return migrationRepository.findDistinctAgeGroups()
                .stream()
                .sorted(Comparator.comparingInt(this::ageGroupSortValue))
                .toList();
    }

    public List<Integer> getAllYears() {
        return migrationRepository.findDistinctYears();
    }

    private int ageGroupSortValue(String ageGroup) {
        if ("100+".equals(ageGroup)) {
            return 100;
        }
        int dashIndex = ageGroup.indexOf('-');
        if (dashIndex > 0) {
            return Integer.parseInt(ageGroup.substring(0, dashIndex));
        }
        return Integer.MAX_VALUE;
    }
}
