package se.scb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.scb.model.Migration;

@Getter
@AllArgsConstructor
public class MigrationResponse {
    private Long id;
    private String region;
    private String regionCode;
    private String gender;
    private String ageGroup;
    private Integer year;
    private Integer immigrations;
    private Integer emigrations;
    private Integer netMigration;

    public static MigrationResponse fromEntity(Migration m) {
        return new MigrationResponse(
                m.getId(),
                m.getRegion(),
                m.getRegionCode(),
                m.getGender(),
                m.getAgeGroup(),
                m.getYear(),
                m.getImmigrations(),
                m.getEmigrations(),
                m.getImmigrations() - m.getEmigrations()
        );
    }
}
