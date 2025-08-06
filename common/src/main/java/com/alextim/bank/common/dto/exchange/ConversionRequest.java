package com.alextim.bank.common.dto.exchange;

import com.alextim.bank.common.validation.ValidCurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
public class ConversionRequest {

    @NotBlank(message = "{conversion.sourceCurrency.notblank}")
    @ValidCurrencyCode
    private String sourceCurrency;

    @NotBlank(message = "{conversion.targetCurrency.notblank}")
    @ValidCurrencyCode
    private String targetCurrency;

    @NotNull(message = "{conversion.amount.required}")
    @DecimalMin(value = "0.01", message = "{conversion.amount.positive}")
    private BigDecimal amount;
}
