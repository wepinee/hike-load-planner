package ru.hikeload.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.hikeload.domain.Hike;

public interface HikeRepository extends JpaRepository<Hike, Long> {

    @Query("""
            select h from Hike h
            where h.organizer.id = :userId
               or exists (
                   select 1 from Participant p
                   where p.hike = h and p.user.id = :userId
               )
            order by h.startDate desc
            """)
    Page<Hike> findAccessibleByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            select h from Hike h
            where h.organizer.id = :userId and h.id <> :currentHikeId
            order by h.startDate desc
            """)
    Page<Hike> findCopySources(
            @Param("userId") Long userId,
            @Param("currentHikeId") Long currentHikeId,
            Pageable pageable
    );
}
