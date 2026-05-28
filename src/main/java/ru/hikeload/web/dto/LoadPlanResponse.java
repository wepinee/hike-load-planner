package ru.hikeload.web.dto;

import ru.hikeload.domain.Assignment;
import ru.hikeload.domain.AssignmentType;
import ru.hikeload.domain.LoadPlan;

import java.time.LocalDateTime;
import java.util.List;

public record LoadPlanResponse(
        Long id,
        LocalDateTime createdAt,
        Integer version,
        List<AssignmentLine> assignments,
        List<ParticipantLoadSummary> summary
) {
    public record AssignmentLine(
            Long id,
            Long participantId,
            String participantName,
            AssignmentType assignmentType,
            String itemName,
            Long gearItemId,
            Long foodItemId,
            Double effectiveWeightKg
    ) {
        public static AssignmentLine from(Assignment a) {
            return new AssignmentLine(
                    a.getId(),
                    a.getParticipant().getId(),
                    a.getParticipant().getName(),
                    a.getAssignmentType() != null ? a.getAssignmentType() : AssignmentType.GEAR_SHARED,
                    a.getItemName() != null ? a.getItemName() : "—",
                    a.getGearItem() != null ? a.getGearItem().getId() : null,
                    a.getFoodItem() != null ? a.getFoodItem().getId() : null,
                    a.getEffectiveWeight()
            );
        }
    }

    public record ParticipantLoadSummary(
            Long participantId,
            String participantName,
            Double totalWeightKg,
            Double maxWeightKg,
            boolean withinLimit
    ) {
    }

    public static LoadPlanResponse from(LoadPlan plan, ru.hikeload.domain.Hike hike) {
        List<AssignmentLine> lines = plan.getAssignments().stream()
                .map(AssignmentLine::from)
                .toList();

        List<ParticipantLoadSummary> summary = hike.getParticipants().stream()
                .map(p -> {
                    double load = plan.getTotalWeightForParticipant(p);
                    double max = p.getWeightLimit().getMaxKg();
                    return new ParticipantLoadSummary(
                            p.getId(), p.getName(), load, max, load <= max
                    );
                })
                .toList();

        return new LoadPlanResponse(
                plan.getId(),
                plan.getCreatedAt(),
                plan.getVersion(),
                lines,
                summary
        );
    }
}
