package se.scb.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import se.scb.dto.MigrationResponse;
import se.scb.model.Migration;
import se.scb.repository.MigrationRepository;
import se.scb.repository.MigrationSpecification;

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
        return migrationRepository.findDistinctAgeGroups();
    }

    public List<Integer> getAllYears() {
        return migrationRepository.findDistinctYears();
    }
}
