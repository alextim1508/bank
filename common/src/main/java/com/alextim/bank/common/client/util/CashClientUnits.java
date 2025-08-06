package com.alextim.bank.common.client.util;

import com.alextim.bank.common.client.CashServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.cash.DepositRequest;
import com.alextim.bank.common.dto.cash.DepositResponse;
import com.alextim.bank.common.dto.cash.WithdrawRequest;
import com.alextim.bank.common.dto.cash.WithdrawResponse;
import com.alextim.bank.common.exception.ConvertClientException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class CashClientUnits {

    public static DepositResponse deposit(CashServiceClient client,
                                          DepositRequest request) {

        log.info("Send 'deposit' request to exchange service");
        var response = client.deposit(request);
        log.info("Response of 'deposit': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new ConvertClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }

    public static WithdrawResponse withdraw(CashServiceClient client,
                                            WithdrawRequest request) {

        log.info("Send 'withdraw' request to exchange service");
        var response = client.withdraw(request);
        log.info("Response of 'withdraw': {}", response);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().getData();
        } else {
            ApiResponse.ApiError error = response.getBody().getError();
            throw new ConvertClientException(error.getMessage(), response.getStatusCode().toString());
        }
    }
}
