package com.alextim.bank.common.dto.exchange;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CurrencyResponse {
    private String title;
    private String code;
}
