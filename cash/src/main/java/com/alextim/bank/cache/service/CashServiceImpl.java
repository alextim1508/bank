package com.alextim.bank.cache.service;

import com.alextim.bank.cache.entity.CashOperation;
import com.alextim.bank.cache.exception.SuspiciousOperationException;
import com.alextim.bank.cache.repository.CashRepository;
import com.alextim.bank.common.client.*;
import com.alextim.bank.common.dto.balance.ApproveOperationRequest;
import com.alextim.bank.common.dto.balance.CreditBalanceRequest;
import com.alextim.bank.common.dto.balance.DebitBalanceRequest;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import com.alextim.bank.common.dto.cash.DepositRequest;
import com.alextim.bank.common.dto.cash.WithdrawRequest;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.alextim.bank.cache.constant.CashOperationType.*;
import static com.alextim.bank.common.client.util.BalanceClientUtils.*;
import static com.alextim.bank.common.client.util.NotificationClientUtils.sendNotification;
import static com.alextim.bank.common.constant.AggregateType.CASH_OPERATION;
import static com.alextim.bank.common.constant.EventType.BALANCE_CREDITED;
import static com.alextim.bank.common.constant.EventType.BALANCE_DEBITED;
import static com.alextim.bank.common.constant.OperationType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {

    private final AccountBalanceServiceClient accountBalanceServiceClient;
    private final BlockerServiceClient blockerServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    private final CashRepository cashRepository;

    private final ATMService atmService;

    @Override
    public void deposit(DepositRequest request) {
        log.info("Deposit request: {}", request);

        var operationCheckRequest = OperationCheckRequest.builder()
                .login(request.getLogin())
                .amount(request.getAmount())
                .operationType(CASH_DEPOSIT)
                .timestamp(LocalDateTime.now())
                .build();
        log.info("Operation check request: {}", operationCheckRequest);

        var operationCheckResponse = checkOperation(blockerServiceClient, operationCheckRequest);
        log.info("Operation check response: {}", operationCheckResponse);
        if (!operationCheckResponse.isApproved()) {
            throw new SuspiciousOperationException();
        }


        var creditBalanceRequest = CreditBalanceRequest.builder()
                .login(request.getLogin())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .build();
        log.info("Credit balance request: {}", creditBalanceRequest);
        var creditBalanceResponse = creditBalance(accountBalanceServiceClient, creditBalanceRequest);
        log.info("Credit balance response: {}", creditBalanceResponse);

        atmService.depositCash(request.getAmount());

        var approveOperationRequest = new ApproveOperationRequest(List.of(request.getLogin()));
        log.info("Approve operation request: {}", approveOperationRequest);
        var approveOperationResponse = approveBalance(accountBalanceServiceClient, approveOperationRequest);
        log.info("Approve operation response: {}", approveOperationResponse);

        var savedCashOperation = cashRepository.save(CashOperation.builder()
                .login(request.getLogin())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .operationType(DEPOSIT)
                .build());
        log.info("Saved cash operation: {}", savedCashOperation);

        sendNotification(notificationServiceClient,
                new NotificationRequest(CASH_OPERATION, BALANCE_CREDITED, request.getLogin(),
                        "Счёт пополнен"));
    }


    @Override
    public void withdraw(WithdrawRequest request) {
        log.info("Deposit request: {}", request);

        var operationCheckRequest = OperationCheckRequest.builder()
                .login(request.getLogin())
                .amount(request.getAmount())
                .operationType(CASH_WITHDRAW)
                .timestamp(LocalDateTime.now())
                .build();
        log.info("Operation check request: {}", operationCheckRequest);

        var operationCheckResponse = checkOperation(blockerServiceClient, operationCheckRequest);
        log.info("Operation check response: {}", operationCheckResponse);
        if (!operationCheckResponse.isApproved()) {
            throw new SuspiciousOperationException();
        }

        var debitBalanceRequestDto = DebitBalanceRequest.builder()
                .login(request.getLogin())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .build();
        log.info("Debit balance request: {}", debitBalanceRequestDto);
        var debitBalanceResponse = debitBalance(accountBalanceServiceClient, debitBalanceRequestDto);
        log.info("Debit balance response: {}", debitBalanceResponse);
        atmService.withdrawCash(request.getAmount());

        var approveOperationRequest = new ApproveOperationRequest(List.of(request.getLogin()));
        log.info("Approve operation request: {}", approveOperationRequest);
        var approveOperationResponse = approveBalance(accountBalanceServiceClient, approveOperationRequest);
        log.info("Approve operation response: {}", approveOperationResponse);

        var savedCashOperation = cashRepository.save(CashOperation.builder()
                .login(request.getLogin())
                .currency(request.getCurrency())
                .amount(request.getAmount())
                .operationType(WITHDRAW)
                .build());
        log.info("Saved cash operation: {}", savedCashOperation);

        sendNotification(notificationServiceClient, new NotificationRequest(CASH_OPERATION, BALANCE_DEBITED,
                request.getLogin(), "Средства сняты"));
    }
}