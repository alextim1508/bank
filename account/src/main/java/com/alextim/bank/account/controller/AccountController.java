package com.alextim.bank.account.controller;


import com.alextim.bank.account.exception.AccountLockedException;
import com.alextim.bank.account.exception.AccountNotFoundException;
import com.alextim.bank.account.exception.BalanceNotOpenedException;
import com.alextim.bank.account.exception.ContactAlreadyExistsException;
import com.alextim.bank.account.service.AccountService;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.account.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountFullResponse>> createAccount(@Valid @RequestBody AccountRequest request) {
        log.info("Incoming request to create account with login: {}", request.getLogin());

        AccountFullResponse response = accountService.createAccount(request);
        log.info("Successfully created account with login: {}", response.getLogin());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<AccountFullResponse>> editAccount(@Valid @RequestBody AccountUpdateRequest request) {
        log.info("Incoming request to update account with login: {}", request.getLogin());

        AccountFullResponse response = accountService.updateAccount(request);
        log.info("Successfully updated account with login: {}", response.getLogin());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<AccountFullResponse>> editAccountPassword(@Valid @RequestBody AccountPasswordUpdateRequest request) {
        log.info("Incoming request to change password for account with login: {}", request.getLogin());

        AccountFullResponse response = accountService.updatePassword(request);
        log.info("Successfully changed password for account with login: {}", response.getLogin());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        log.info("Incoming request to get all accounts");

        List<AccountResponse> response = accountService.getAllAccounts();
        log.info("Retrieved {} accounts", response.size());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<AccountFullResponse>> getAccount(@RequestParam String login) {
        log.info("Incoming request to search account by login: {}", login);

        AccountFullResponse response = accountService.getAccountByLogin(login);
        log.info("Found account: {}", login);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<AccountStatusResponse>> getAccountStatus(@RequestParam String login) {
        log.info("Incoming request to get status for account with login: {}", login);

        AccountStatusResponse response = accountService.getAccountStatus(login);
        log.info("Retrieved status for account with login {}", login);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/contacts")
    ResponseEntity<ApiResponse<AccountContactsResponse>> getAccountContacts(@RequestParam String login) {
        log.info("Incoming request to get contacts for account with login: {}", login);

        AccountContactsResponse response = accountService.getAccountContacts(login);
        log.info("Retrieved {} contacts for account with login {}", response.getContacts().size(), login);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountNotFoundException(AccountNotFoundException ex) {
        log.warn("Account not found: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error("Account not found", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountLockedException(AccountLockedException ex) {
        log.warn("Account is locked: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error("Account is locked", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BalanceNotOpenedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountNotOpenedException(BalanceNotOpenedException ex) {
        log.warn("Account balance not opened: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error("Account balance not opened", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ContactAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleContactAlreadyExistsException(ContactAlreadyExistsException ex) {
        log.warn("Contact already exists: {}", ex.getMessage());

        ApiResponse<?> response = ApiResponse.error("Contact already exists", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}