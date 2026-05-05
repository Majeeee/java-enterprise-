package se.scb.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import se.scb.dto.MigrationResponse;
import se.scb.model.Migration;
import se.scb.repository.MigrationRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class MigrationServiceTest {

    @Mock private MigrationRepository migrationRepository;

    @InjectMocks
    private MigrationService migrationService;

    @Test
    void findWithFilters_returnsCorrectlyMappedResponses() {
        Migration m1 = new Migration("Stockholms län", "01", "men", "20-29", 2020, 1500, 1200);
        Migration m2 = new Migration("Skåne län",      "12", "women", "30-39", 2021, 900,  800);

        when(migrationRepository.findAll(any(Specification.class))).thenReturn(List.of(m1, m2));

        List<MigrationResponse> result = migrationService.findWithFilters(null, null, null, null);

        assertThat(result).hasSize(2);

        MigrationResponse r1 = result.get(0);
        assertThat(r1.getRegion()).isEqualTo("Stockholms län");
        assertThat(r1.getImmigrations()).isEqualTo(1500);
        assertThat(r1.getEmigrations()).isEqualTo(1200);
        assertThat(r1.getNetMigration()).isEqualTo(300);

        MigrationResponse r2 = result.get(1);
        assertThat(r2.getRegion()).isEqualTo("Skåne län");
        assertThat(r2.getNetMigration()).isEqualTo(100);
    }

    @Test
    void findWithFilters_withYearFilter_passesSpecificationToRepository() {
        when(migrationRepository.findAll(any(Specification.class))).thenReturn(List.of());

        migrationService.findWithFilters("Stockholms län", "men", "20-29", 2022);

        verify(migrationRepository).findAll(any(Specification.class));
    }

    @Test
    void getAllRegions_delegatesToRepository() {
        when(migrationRepository.findDistinctRegions())
                .thenReturn(List.of("Stockholms län", "Skåne län"));

        List<String> regions = migrationService.getAllRegions();

        assertThat(regions).containsExactly("Stockholms län", "Skåne län");
        verify(migrationRepository).findDistinctRegions();
    }

    @Test
    void getAllYears_delegatesToRepository() {
        when(migrationRepository.findDistinctYears()).thenReturn(List.of(2020, 2021, 2022));

        List<Integer> years = migrationService.getAllYears();

        assertThat(years).containsExactly(2020, 2021, 2022);
    }
}
