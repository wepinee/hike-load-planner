package ru.hikeload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.hikeload.domain.Participant;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findByHikeId(Long hikeId);

    Optional<Participant> findByIdAndHikeId(Long id, Long hikeId);

    boolean existsByHikeIdAndEmail(Long hikeId, String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Participant p where p.hike.id = :hikeId")
    void deleteByHikeId(@Param("hikeId") Long hikeId);
}
