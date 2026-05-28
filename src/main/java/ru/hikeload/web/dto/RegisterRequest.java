package ru.hikeload.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Укажите email")
        @Email(message = "Некорректный email")
        String email,
        @NotBlank(message = "Укажите имя")
        @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
        String displayName,
        @NotBlank(message = "Укажите пароль")
        @Size(min = 4, max = 100, message = "Пароль должен быть от 4 до 100 символов")
        String password
) {
}
