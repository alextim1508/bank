package com.alextim.bank.exchangegenerator.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class OAuthTokenResponse {
    private String access_token;
    private int expires_in;
    private String token_type;

    public String getAccessToken() {
        return access_token;
    }
}