package com.alextim.bank.common.dto.balance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@Getter
@ToString
public class DebitBalanceRequest {

    @NotBlank(message = "{account.login.notblank}")
    @Size(min = 4, max = 20, message = "{account.login.size}")
    private String login;

    @NotBlank(message = "{balance.currency.required}")
    @Size(min = 3, max = 3, message = "{balance.currency.size}")
    private String currency;

    @NotNull(message = "{balance.amount.required}")
    @DecimalMin(value = "0.01", message = "{balance.amount.min}")
    private BigDecimal amount;
}
