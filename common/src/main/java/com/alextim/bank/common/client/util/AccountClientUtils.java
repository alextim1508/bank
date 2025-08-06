package com.alextim.bank.common.client.util;

import com.alextim.bank.common.client.AccountServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.account.*;
import com.alextim.bank.common.exception.AccountClientException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class AccountClientUtils {

    public static AccountFullResponse createAccount(AccountServiceClient client,
                                                    AccountRequest request) {

        log.info("Send 'createAccount' request to account service: {}", request);
        var response = client.createAccount(request);
        log.info("Response of 'createAccount': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static AccountUpdateResponse updateAccount(AccountServiceClient client,
                                                      AccountUpdateRequest request) {

        log.info("Send 'updateAccount' request to account service: {}", request);
        var response = client.editAccount(request);
        log.info("Response of 'updateAccount': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static AccountPasswordUpdateResponse updateAccountPassword(AccountServiceClient client,
                                                                      AccountPasswordUpdateRequest request) {

        log.info("Send 'updateAccountPassword' request to account service: {}", request);
        var response = client.editPassword(request);
        log.info("Response of 'updateAccountPassword': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static List<AccountResponse> getAccounts(AccountServiceClient client) {

        log.info("Send 'getAccounts' request to account service");
        var response = client.getAllAccounts();
        log.info("Response of 'getAccounts': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static AccountFullResponse getAccountByLogin(AccountServiceClient client,
                                                        String login) {

        log.info("Send 'getAccount' request to account service for login: {}", login);
        var response = client.getAccount(login);
        log.info("Response of 'getAccount': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static AccountStatusResponse getAccountStatus(AccountServiceClient client,
                                                         String login) {

        log.info("Send 'getAccountStatus' request to account service for login: {}", login);
        var response = client.getAccountStatus(login);
        log.info("Response of 'getAccountStatus': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static AccountContactsResponse getAccountContacts(AccountServiceClient client,
                                                             String login) {

        log.info("Send 'getAccountContacts' request to account service for login: {}", login);
        var response = client.getAccountContacts(login);
        log.info("Response of 'getAccountContacts': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }
}
