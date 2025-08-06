package com.alextim.bank.common.dto.transfer;


import com.alextim.bank.common.validation.ValidCurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ExternalTransferRequest {

    @NotBlank(message = "{transfer.fromLogin.notblank}")
    @Size(min = 4, max = 20, message = "{transfer.fromLogin.size}")
    private String fromLogin;

    @NotBlank(message = "{transfer.fromCurrency.notblank}")
    @ValidCurrencyCode
    private String fromCurrency;

    @NotBlank(message = "{transfer.toLogin.notblank}")
    @Size(min = 4, max = 20, message = "{transfer.toLogin.size}")
    private String toLogin;

    @NotBlank(message = "{transfer.toCurrency.notblank}")
    @ValidCurrencyCode
    private String toCurrency;

    @NotNull(message = "{transfer.amount.required}")
    @DecimalMin(value = "0.01", message = "{transfer.amount.positive}")
    private BigDecimal amount;
}
