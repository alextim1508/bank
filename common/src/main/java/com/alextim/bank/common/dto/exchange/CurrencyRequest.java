package com.alextim.bank.common.dto.exchange;

import com.alextim.bank.common.validation.ValidCurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CurrencyRequest {

    @ValidCurrencyCode
    private String code;

    @NotBlank(message = "{currency.rusTitle.notblank}")
    @Size(min = 2, max = 50, message = "{currency.rusTitle.size}")
    private String rusTitle;

    @NotBlank(message = "{currency.title.notblank}")
    @Size(min = 2, max = 50, message = "{currency.title.size}")
    private String title;

    @NotBlank(message = "{currency.country.notblank}")
    @Size(min = 2, max = 100, message = "{currency.country.size}")
    private String country;

    @NotBlank(message = "{currency.mark.notblank}")
    @Size(min = 1, max = 5, message = "{currency.mark.size}")
    private String mark;
}
