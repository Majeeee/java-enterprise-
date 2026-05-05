package se.scb.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import se.scb.model.Migration;

import java.util.ArrayList;
import java.util.List;

public class MigrationSpecification {

    private MigrationSpecification() {
        // Utility-klass, ska inte instansieras
    }

    public static Specification<Migration> withFilters(String region, String gender,
                                                        String ageGroup, Integer year) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (region != null && !region.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("region")),
                        "%" + region.toLowerCase() + "%"));
            }

            if (gender != null && !gender.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("gender")),
                        gender.toLowerCase()));
            }

            if (ageGroup != null && !ageGroup.isBlank()) {
                predicates.add(cb.equal(root.get("ageGroup"), ageGroup));
            }

            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
