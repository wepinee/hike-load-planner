package ru.hikeload.web.dto;

import ru.hikeload.domain.Gender;

public record SuggestedWeightResponse(
        Gender gender,
        Double suggestedMaxKg,
        String note
) {
}
