package ru.hikeload.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddFoodItemRequest(
        @NotBlank(message = "{food.name.required}")
        String name,
        @NotNull @Positive(message = "{food.weight.positive}")
        Double weightPerPortionKg,
        Integer caloriesPerPortion,
        @NotNull @Positive(message = "{food.portions.positive}")
        Double portionsPerPersonPerDay
) {
}
