package ru.hikeload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.hikeload.domain.FoodItem;

import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByHikeIdOrderByNameAsc(Long hikeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from FoodItem f where f.hike.id = :hikeId")
    void deleteByHikeId(@Param("hikeId") Long hikeId);
}
