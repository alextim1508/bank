package com.alextim.bank.account.controller;

import com.alextim.bank.account.exception.AccountNotFoundException;
import com.alextim.bank.account.exception.BalanceNotFoundException;
import com.alextim.bank.account.exception.BalanceNotOpenedException;
import com.alextim.bank.account.exception.InsufficientFundsException;
import com.alextim.bank.account.service.BalanceService;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.balance.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account/balance")
@RequiredArgsConstructor
@Slf4j
public class BalanceController {

    private final BalanceService balanceService;

    @PostMapping("/debit")
    public ResponseEntity<ApiResponse<DebitBalanceResponse>> debitBalance(@Valid @RequestBody DebitBalanceRequest request) {
        log.info("Incoming DEBIT request:{}", request);

        balanceService.debitBalance(request);
        log.info("Successfully debited {} {} from account {}", request.getAmount(), request.getCurrency(), request.getLogin());


        return ResponseEntity.ok(ApiResponse.success(new DebitBalanceResponse(request.getLogin())));
    }

    @PostMapping("/credit")
    public ResponseEntity<ApiResponse<CreditBalanceResponse>> creditBalance(@Valid @RequestBody CreditBalanceRequest request) {
        log.info("Incoming CREDIT request:{}", request);

        balanceService.creditBalance(request);
        log.info("Successfully credited {} {} to account {}", request.getAmount(), request.getCurrency(), request.getLogin());

        return ResponseEntity.ok(ApiResponse.success(new CreditBalanceResponse(request.getLogin())));
    }

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<ApproveOperationResponse>> approve(@RequestBody ApproveOperationRequest request) {
        log.info("Incoming APPROVE request: {}", request);

        balanceService.approve(request);
        log.info("Successfully approved operation: logins={}", request.getLogins());

        return ResponseEntity.ok(ApiResponse.success(new ApproveOperationResponse(request.getLogins())));
    }

    @ExceptionHandler(BalanceNotOpenedException.class)
    public ResponseEntity<ApiResponse<?>> handleBalanceNotOpenedException(BalanceNotOpenedException ex) {
        log.warn("Balance is not opened: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error("Account not opened", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficientFundsException(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error("Insufficient funds", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountNotFoundException(AccountNotFoundException ex) {
        log.warn("Account not found: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error("Account not found", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BalanceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleBalanceNotFoundException(BalanceNotFoundException ex) {
        log.warn("Balance not found: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error("Balance not found", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}
