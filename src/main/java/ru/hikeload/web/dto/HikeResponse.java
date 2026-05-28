package ru.hikeload.web.dto;

import ru.hikeload.domain.Hike;
import ru.hikeload.domain.HikeStatus;

import java.time.LocalDate;

public record HikeResponse(
        Long id,
        String name,
        LocalDate startDate,
        Integer durationDays,
        HikeStatus status,
        Double startLat,
        Double startLon,
        int participantCount,
        double totalGearWeightKg,
        Long organizerUserId
) {
    public static HikeResponse from(Hike hike) {
        return new HikeResponse(
                hike.getId(),
                hike.getName(),
                hike.getStartDate(),
                hike.getDurationDays(),
                hike.getStatus(),
                hike.getStartLat(),
                hike.getStartLon(),
                hike.getParticipants().size(),
                hike.calculateTotalGearWeight(),
                hike.getOrganizer().getId()
        );
    }
}
