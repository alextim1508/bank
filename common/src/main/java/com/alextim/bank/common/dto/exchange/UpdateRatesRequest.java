package com.alextim.bank.common.dto.exchange;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UpdateRatesRequest {

    @NotEmpty(message = "Rates cannot be empty")
    private Map<String, Double> rates;
}
