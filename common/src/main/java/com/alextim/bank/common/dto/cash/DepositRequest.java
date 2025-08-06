package com.alextim.bank.common.dto.cash;

import com.alextim.bank.common.validation.ValidCurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Getter
@ToString
public class DepositRequest {

    @NotBlank(message = "{deposit.login.notblank}")
    @Size(min = 4, max = 20, message = "{deposit.login.size}")
    private String login;

    @ValidCurrencyCode
    private String currency;

    @NotNull(message = "{deposit.amount.required}")
    @DecimalMin(value = "0.01", message = "{deposit.amount.positive}")
    private BigDecimal amount;
}