package com.alextim.bank.common.client.util;

import com.alextim.bank.common.client.AccountBalanceServiceClient;
import com.alextim.bank.common.client.BlockerServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.balance.*;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import com.alextim.bank.common.dto.blocker.OperationCheckResponse;
import com.alextim.bank.common.exception.AccountBalanceServiceClientException;
import com.alextim.bank.common.exception.BlockerClientException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class BalanceClientUtils {

    public static OperationCheckResponse checkOperation(BlockerServiceClient client,
                                                        OperationCheckRequest request) {

        log.info("Send 'checkOperation' request to blocker service");
        var response = client.checkOperation(request);
        log.info("Response of 'checkOperation': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new BlockerClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static CreditBalanceResponse creditBalance(AccountBalanceServiceClient client,
                                                      CreditBalanceRequest request) {

        log.info("Send 'creditBalanceResponse' request to balance-account service");
        var response = client.creditBalance(request);
        log.info("Response of 'creditBalanceResponse': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountBalanceServiceClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static DebitBalanceResponse debitBalance(AccountBalanceServiceClient client,
                                                    DebitBalanceRequest request) {

        log.info("Send 'debitBalanceResponse' request to balance-account service");
        var response = client.debitBalance(request);
        log.info("Response of 'debitBalanceResponse': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountBalanceServiceClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static ApproveOperationResponse approveBalance(AccountBalanceServiceClient client,
                                                          ApproveOperationRequest request) {

        log.info("Send 'approveBalanceResponse' request to balance-account service");
        var response = client.approve(request);
        log.info("Response of 'approveBalanceResponse': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new AccountBalanceServiceClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }
}
