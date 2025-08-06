package com.alextim.bank.common.client;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.balance.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-balance-service")
public interface AccountBalanceServiceClient {

    @PostMapping("/debit")
    ResponseEntity<ApiResponse<DebitBalanceResponse>> debitBalance(@RequestBody DebitBalanceRequest debitBalanceRequestDto);

    @PostMapping("/credit")
    ResponseEntity<ApiResponse<CreditBalanceResponse>> creditBalance(@RequestBody CreditBalanceRequest creditBalanceRequestDto);

    @PostMapping("/approve")
    ResponseEntity<ApiResponse<ApproveOperationResponse>> approve(@RequestBody ApproveOperationRequest approveOperationRequestDto);
}
