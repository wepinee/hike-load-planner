package ru.hikeload.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.hikeload.domain.Gender;
import ru.hikeload.domain.ParticipantRole;

public record AddParticipantRequest(
        @NotBlank(message = "Имя обязательно")
        String name,
        @Email(message = "Некорректный email")
        String email,
        @NotNull(message = "Укажите пол участника")
        Gender gender,
        @NotNull @Positive(message = "Вес должен быть больше 0")
        Double maxWeightKg,
        ParticipantRole role
) {
}
