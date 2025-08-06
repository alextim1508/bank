package com.alextim.bank.common.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class AccountStatusResponse {
    private String login;
    private Boolean isBlocked;
}
