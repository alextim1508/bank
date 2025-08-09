package com.alextim.bank.common.client;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.balance.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-balance-service")
@Retry(name = "accountBalanceService")
@CircuitBreaker(name = "accountBalanceService")
public interface AccountBalanceServiceClient {

    @PostMapping("/debit")
    ResponseEntity<ApiResponse<DebitBalanceResponse>> debitBalance(@RequestBody DebitBalanceRequest debitBalanceRequestDto);

    @PostMapping("/credit")
    ResponseEntity<ApiResponse<CreditBalanceResponse>> creditBalance(@RequestBody CreditBalanceRequest creditBalanceRequestDto);

    @PostMapping("/approve")
    ResponseEntity<ApiResponse<ApproveOperationResponse>> approve(@RequestBody ApproveOperationRequest approveOperationRequestDto);
}
