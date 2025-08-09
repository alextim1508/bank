package com.alextim.bank.common.client;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.cash.DepositRequest;
import com.alextim.bank.common.dto.cash.DepositResponse;
import com.alextim.bank.common.dto.cash.WithdrawRequest;
import com.alextim.bank.common.dto.cash.WithdrawResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cash-service")
@Retry(name = "cashService")
@CircuitBreaker(name = "cashService")
public interface CashServiceClient {
    @PostMapping("/deposit")
    ResponseEntity<ApiResponse<DepositResponse>> deposit(@RequestBody DepositRequest request);

    @PostMapping("/withdraw")
    ResponseEntity<ApiResponse<WithdrawResponse>> withdraw(@RequestBody WithdrawRequest request);
}