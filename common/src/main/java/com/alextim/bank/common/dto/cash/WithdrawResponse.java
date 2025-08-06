package com.alextim.bank.common.dto.cash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class WithdrawResponse {
    private String login;
}
