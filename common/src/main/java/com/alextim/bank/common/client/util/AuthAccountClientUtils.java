package com.alextim.bank.common.client.util;

import com.alextim.bank.common.client.AuthServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.auth.LoginRequest;
import com.alextim.bank.common.dto.auth.RefreshRequest;
import com.alextim.bank.common.dto.auth.TokenPairResponse;
import com.alextim.bank.common.dto.auth.TokenStatusResponse;
import com.alextim.bank.common.exception.AccountClientException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.alextim.bank.common.service.JwtServiceImpl.maskToken;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class AuthAccountClientUtils {

    public static TokenPairResponse login(AuthServiceClient client,
                                          LoginRequest request) {

        log.info("Send 'login' request to account service: {}", request);
        var response = client.login(request);
        log.info("Response of 'login': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static TokenPairResponse refreshToken(AuthServiceClient client,
                                                 RefreshRequest request) {

        log.info("Send 'refreshToken' request to account service: {}", request);
        var response = client.refresh(request);
        log.info("Response of 'refreshToken': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static TokenStatusResponse checkTokenStatus(AuthServiceClient client,
                                                       String token) {

        log.info("Send 'checkTokenStatus' request to account service: {}", maskToken(token));
        var response = client.checkTokenStatus(token);
        log.info("Response of 'checkTokenStatus': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }
}
