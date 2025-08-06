package com.alextim.bank.common.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString(exclude = "newPassword")
public class AccountPasswordUpdateRequest {
    private String login;
    private String newPassword;
}
