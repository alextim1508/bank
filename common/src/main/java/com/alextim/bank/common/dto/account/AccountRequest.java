package com.alextim.bank.common.dto.account;

import com.alextim.bank.common.validation.AtLeast;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString(exclude = "password")
public class AccountRequest {

    @NotBlank(message = "{account.login.notblank}")
    @Size(min = 4, max = 20, message = "{account.login.size}")
    private String login;

    @NotBlank(message = "{account.password.notblank}")
    @Size(min = 4, max = 100, message = "{account.password.size}")
    private String password;

    @NotBlank(message = "{account.firstname.notblank}")
    private String firstName;

    @NotBlank(message = "{account.lastname.notblank}")
    private String lastName;

    @NotNull(message = "{account.birthdate.required}")
    @Past(message = "{account.birthdate.past}")
    @AtLeast(years = 18, message = "{account.birthdate.atleast}")
    private LocalDate birthDate;

    @Email(message = "{account.email.format}")
    @NotBlank(message = "{account.email.notblank}")
    private String email;

    @NotBlank(message = "{account.telegram.notblank}")
    private String telegram;
}