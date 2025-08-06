package com.alextim.bank.common.dto.account;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class AccountResponse {
    private String login;
    private String name;
}
