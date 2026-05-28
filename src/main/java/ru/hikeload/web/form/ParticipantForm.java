package ru.hikeload.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.hikeload.domain.Gender;

public class ParticipantForm {

    @NotBlank(message = "Имя обязательно")
    private String name;

    @Email(message = "Некорректный email")
    private String email;

    @NotNull(message = "Укажите пол")
    private Gender gender;

    @NotNull(message = "Укажите максимальный вес")
    @Positive(message = "Вес должен быть больше 0")
    private Double maxWeightKg;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Double getMaxWeightKg() {
        return maxWeightKg;
    }

    public void setMaxWeightKg(Double maxWeightKg) {
        this.maxWeightKg = maxWeightKg;
    }
}
