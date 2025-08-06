package com.alextim.bank.front.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SignUpForm(
        @NotBlank(message = "Логин не может быть пустым") String login,
        @NotBlank(message = "Пароль не может быть пустым") String password,
        @NotBlank(message = "Имя и фамилия обязательны") String name,
        @Email(message = "Некорректный email") @NotBlank String email,
        @NotBlank(message = "Подтверждение пароля не может быть пустым") String confirmPassword,
        @NotBlank(message = "Telegram не может быть пустым") String telegram,
        @NotNull(message = "Дата рождения обязательна") LocalDate birthdate
) {}