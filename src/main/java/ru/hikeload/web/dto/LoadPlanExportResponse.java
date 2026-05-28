package ru.hikeload.web.dto;

import ru.hikeload.domain.Hike;

import java.time.LocalDateTime;
import java.util.List;

public record LoadPlanExportResponse(
        Long hikeId,
        String hikeName,
        LocalDateTime exportedAt,
        Integer planVersion,
        List<ParticipantExport> participants
) {
    public record ParticipantExport(
            String name,
            String email,
            Double maxWeightKg,
            Double totalWeightKg,
            List<ItemExport> items
    ) {
    }

    public record ItemExport(
            String name,
            String type,
            Double weightKg
    ) {
    }

    public static LoadPlanExportResponse from(Hike hike, LoadPlanResponse plan) {
        List<ParticipantExport> participants = plan.summary().stream()
                .map(s -> {
                    List<ItemExport> items = plan.assignments().stream()
                            .filter(a -> a.participantId().equals(s.participantId()))
                            .map(a -> new ItemExport(
                                    a.itemName(),
                                    a.assignmentType().name(),
                                    a.effectiveWeightKg()
                            ))
                            .toList();
                    String email = hike.getParticipants().stream()
                            .filter(p -> p.getId().equals(s.participantId()))
                            .map(p -> p.getEmail())
                            .findFirst()
                            .orElse(null);
                    return new ParticipantExport(
                            s.participantName(),
                            email,
                            s.maxWeightKg(),
                            s.totalWeightKg(),
                            items
                    );
                })
                .toList();

        return new LoadPlanExportResponse(
                hike.getId(),
                hike.getName(),
                LocalDateTime.now(),
                plan.version(),
                participants
        );
    }
}
