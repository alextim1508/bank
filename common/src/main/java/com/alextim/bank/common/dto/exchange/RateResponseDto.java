package com.alextim.bank.common.dto.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class RateResponseDto {
    public String code;
    public String title;

    @JsonProperty("value")
    public double exchangeRateToRub;
}
