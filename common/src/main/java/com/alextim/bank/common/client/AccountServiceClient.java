package com.alextim.bank.common.client;

import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.account.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "account-service")
@Retry(name = "accountService")
@CircuitBreaker(name = "accountService")
public interface AccountServiceClient {

    @PostMapping
    ResponseEntity<ApiResponse<AccountFullResponse>> createAccount(@RequestBody AccountRequest request);

    @PutMapping
    ResponseEntity<ApiResponse<AccountUpdateResponse>> editAccount(@RequestBody AccountUpdateRequest request);

    @PutMapping("/password")
    ResponseEntity<ApiResponse<AccountPasswordUpdateResponse>> editPassword(@RequestBody AccountPasswordUpdateRequest request);

    @GetMapping
    ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts();

    @GetMapping("/search")
    ResponseEntity<ApiResponse<AccountFullResponse>> getAccount(@RequestParam String login);

    @GetMapping("/status")
    ResponseEntity<ApiResponse<AccountStatusResponse>> getAccountStatus(@RequestParam String login);

    @GetMapping("/contacts")
    ResponseEntity<ApiResponse<AccountContactsResponse>> getAccountContacts(@RequestParam String login);
}
