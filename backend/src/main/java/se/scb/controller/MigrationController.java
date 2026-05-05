package se.scb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import se.scb.dto.MigrationResponse;
import se.scb.service.MigrationService;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    private final MigrationService migrationService;

    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<MigrationResponse>> getMigrations(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) Integer year) {

        List<MigrationResponse> result = migrationService.findWithFilters(region, gender, ageGroup, year);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filters")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        return ResponseEntity.ok(Map.of(
                "regions", migrationService.getAllRegions(),
                "ageGroups", migrationService.getAllAgeGroups(),
                "years", migrationService.getAllYears()
        ));
    }
}
