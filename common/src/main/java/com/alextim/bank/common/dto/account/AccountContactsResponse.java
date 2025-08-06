package com.alextim.bank.common.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccountContactsResponse {
    private String login;
    private Map<String, String> contacts;
}
