package com.alextim.bank.common.dto.account;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountBalanceResponse {
    private String code;
    private String title;
    private boolean opened;
    private BigDecimal amount;
}