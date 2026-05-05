package se.scb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.scb.model.Migration;

import java.util.List;

@Repository
public interface MigrationRepository extends JpaRepository<Migration, Long>,
        JpaSpecificationExecutor<Migration> {

    @Query("SELECT DISTINCT m.region FROM Migration m ORDER BY m.region")
    List<String> findDistinctRegions();

    @Query("SELECT DISTINCT m.ageGroup FROM Migration m ORDER BY m.ageGroup")
    List<String> findDistinctAgeGroups();

    @Query("SELECT DISTINCT m.year FROM Migration m ORDER BY m.year")
    List<Integer> findDistinctYears();
}
