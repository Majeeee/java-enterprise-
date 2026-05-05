package se.scb.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "migrations")
@Getter
@Setter
@NoArgsConstructor
public class Migration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String regionCode;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String ageGroup;

    @Column(nullable = false)
    private Integer year;

    // Antal inflyttade
    @Column(nullable = false)
    private Integer immigrations;

    // Antal utflyttade
    @Column(nullable = false)
    private Integer emigrations;

    public Migration(String region, String regionCode, String gender,
                     String ageGroup, Integer year, Integer immigrations, Integer emigrations) {
        this.region = region;
        this.regionCode = regionCode;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.year = year;
        this.immigrations = immigrations;
        this.emigrations = emigrations;
    }
}
