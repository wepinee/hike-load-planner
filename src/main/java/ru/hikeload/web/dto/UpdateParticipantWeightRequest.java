package ru.hikeload.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateParticipantWeightRequest(
        @NotNull @Positive(message = "{participant.weight.positive}")
        Double maxWeightKg
) {
}
