package ru.hikeload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.hikeload.domain.GearItem;
import ru.hikeload.domain.ItemType;

import java.util.List;

public interface GearItemRepository extends JpaRepository<GearItem, Long> {

    @Query("SELECT g FROM GearItem g LEFT JOIN FETCH g.owner WHERE g.hike.id = :hikeId ORDER BY g.name")
    List<GearItem> findByHikeIdWithOwner(@Param("hikeId") Long hikeId);

    default List<GearItem> findByHikeIdOrderByNameAsc(Long hikeId) {
        return findByHikeIdWithOwner(hikeId);
    }

    List<GearItem> findByHikeIdAndType(Long hikeId, ItemType type);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from GearItem g where g.hike.id = :hikeId")
    void deleteByHikeId(@Param("hikeId") Long hikeId);
}
