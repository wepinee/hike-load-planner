package ru.hikeload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.hikeload.domain.LoadPlan;

import java.util.Optional;

public interface LoadPlanRepository extends JpaRepository<LoadPlan, Long> {

    Optional<LoadPlan> findByHikeId(Long hikeId);

    void deleteByHikeId(Long hikeId);

    /**
     * Нельзя JOIN FETCH и participants, и assignments в одном запросе (две коллекции List).
     * Участники передаются отдельно из уже загруженного Hike.
     */
    @Query("""
            SELECT DISTINCT lp FROM LoadPlan lp
            LEFT JOIN FETCH lp.assignments a
            LEFT JOIN FETCH a.participant
            LEFT JOIN FETCH a.gearItem
            LEFT JOIN FETCH a.foodItem
            WHERE lp.hike.id = :hikeId
            """)
    Optional<LoadPlan> findDetailedByHikeId(@Param("hikeId") Long hikeId);
}
