package com.alextim.bank.common.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TokenPairResponse {
    private String accessToken;
    private String refreshToken;
}
