package ru.hikeload.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateFoodItemRequest(
        @NotBlank(message = "Введите название продукта")
        String name,
        @NotNull @Positive(message = "Вес порции должен быть больше 0")
        Double weightPerPortionKg,
        Integer caloriesPerPortion,
        @NotNull @Positive(message = "Укажите число порций больше 0")
        Double portionsPerPersonPerDay
) {
}
