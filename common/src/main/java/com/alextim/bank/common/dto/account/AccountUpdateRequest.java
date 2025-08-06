package com.alextim.bank.common.dto.account;

import com.alextim.bank.common.validation.AtLeast;
import com.alextim.bank.common.validation.NameFormat;
import com.alextim.bank.common.validation.ValidCurrencyCodes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class AccountUpdateRequest {

    private String login;

    @NameFormat
    private String name;

    @NotNull(message = "{account.birthdate.required}")
    @Past(message = "{account.birthdate.past}")
    @AtLeast(years = 18, message = "{account.birthdate.atleast}")
    private LocalDate birthDate;

    @ValidCurrencyCodes
    private List<String> currencyCodes;
}
