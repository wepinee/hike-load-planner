package ru.hikeload.web.dto;

import jakarta.validation.constraints.NotNull;

public record ReassignItemRequest(
        @NotNull Long assignmentId,
        @NotNull Long toParticipantId
) {
}
