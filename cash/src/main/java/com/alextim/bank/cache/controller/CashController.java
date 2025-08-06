package com.alextim.bank.cache.controller;

import com.alextim.bank.cache.exception.SuspiciousOperationException;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.cash.DepositRequest;
import com.alextim.bank.common.dto.cash.DepositResponse;
import com.alextim.bank.common.dto.cash.WithdrawRequest;
import com.alextim.bank.cache.service.CashServiceImpl;
import com.alextim.bank.common.dto.cash.WithdrawResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/cash")
@RequiredArgsConstructor
@Slf4j
public class CashController {

    private final CashServiceImpl cashService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<DepositResponse>> deposit(@Valid @RequestBody DepositRequest request) {
        log.info("deposit: {}", request);

        cashService.deposit(request);

        DepositResponse response = new DepositResponse(request.getLogin());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WithdrawResponse>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        log.info("withdraw: {}", request);

        cashService.withdraw(request);

        WithdrawResponse response = new WithdrawResponse(request.getLogin());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @ExceptionHandler(SuspiciousOperationException.class)
    public ResponseEntity<ApiResponse<?>> handleSuspiciousOperationException(SuspiciousOperationException ex) {
        log.error("HandleSuspiciousOperationException", ex);

        ApiResponse<?> response = ApiResponse.error("Suspicious operation", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}
