package ru.hikeload.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateHikeRequest(
        @NotBlank(message = "Название не может быть пустым")
        String name,
        @NotNull @FutureOrPresent(message = "Дата не может быть в прошлом")
        LocalDate startDate,
        @NotNull @Min(value = 1, message = "Длительность должна быть больше 0")
        Integer durationDays,
        Double startLat,
        Double startLon
) {
}
