package ru.hikeload.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateGearItemRequest(
        @NotBlank(message = "Введите название предмета")
        String name,
        @NotNull @Positive(message = "Вес должен быть больше 0")
        Double weightKg
) {
}
