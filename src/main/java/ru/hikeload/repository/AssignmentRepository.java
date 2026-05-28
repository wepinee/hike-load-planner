package ru.hikeload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.hikeload.domain.Assignment;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByLoadPlanHikeIdAndParticipantId(Long hikeId, Long participantId);

    @Modifying
    @Query("delete from Assignment a where a.foodItem.id = :foodItemId")
    void deleteByFoodItemId(@Param("foodItemId") Long foodItemId);

    @Modifying
    @Query("delete from Assignment a where a.gearItem.id = :gearItemId")
    void deleteByGearItemId(@Param("gearItemId") Long gearItemId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Assignment a where a.loadPlan.hike.id = :hikeId")
    void deleteByHikeId(@Param("hikeId") Long hikeId);
}
