package com.alextim.bank.common.dto.auth;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class LoginRequest {
    private String login;
    private String password;
}
