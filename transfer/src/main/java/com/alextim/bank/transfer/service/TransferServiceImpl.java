package com.alextim.bank.transfer.service;

import com.alextim.bank.common.client.AccountBalanceServiceClient;
import com.alextim.bank.common.client.BlockerServiceClient;
import com.alextim.bank.common.client.ExchangeServiceClient;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.dto.balance.*;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import com.alextim.bank.common.dto.exchange.ConversionRequest;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.common.dto.transfer.ExternalTransferRequest;
import com.alextim.bank.common.dto.transfer.InternalTransferRequest;
import com.alextim.bank.transfer.entity.TransferOperation;
import com.alextim.bank.transfer.exception.SuspiciousOperationException;
import com.alextim.bank.transfer.repository.TransferOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.alextim.bank.common.client.util.BalanceClientUtils.*;
import static com.alextim.bank.common.client.util.ExchangeClientUtils.convertAmount;
import static com.alextim.bank.common.client.util.NotificationClientUtils.sendNotification;
import static com.alextim.bank.common.constant.AggregateType.TRANSFER;
import static com.alextim.bank.common.constant.EventType.BALANCE_TRANSFERRED_EXTERNAL;
import static com.alextim.bank.common.constant.EventType.BALANCE_TRANSFERRED_INTERNAL;
import static com.alextim.bank.common.constant.OperationType.*;
import static com.alextim.bank.transfer.constant.TransferOperationType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final AccountBalanceServiceClient accountBalanceServiceClient;
    private final ExchangeServiceClient exchangeServiceClient;
    private final BlockerServiceClient blockerServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    private final TransferOperationRepository transferOperationRepository;

    @Override
    public void internalTransfer(InternalTransferRequest request) {
        log.info("Internal transfer request: {}", request);

        BigDecimal debitAmount = request.getAmount();
        Double exchangeRateToRub = null;
        if (!request.getFromCurrency().equals(request.getToCurrency())) {
            log.info("From currency and To currency are not the same");
            var conversionRequest = ConversionRequest.builder()
                    .amount(request.getAmount())
                    .sourceCurrency(request.getFromCurrency())
                    .targetCurrency(request.getToCurrency())
                    .build();
            log.info("Conversion request: {}", conversionRequest);

            var conversionResponse = convertAmount(exchangeServiceClient, conversionRequest);
            log.info("Convert response: {}", conversionResponse);

            debitAmount = conversionResponse.getConvertedAmount();
            exchangeRateToRub = conversionResponse.getExchangeRateToRub();
        }

        var operationCheckRequest = OperationCheckRequest.builder()
                .login(request.getLogin())
                .amount(debitAmount)
                .operationType(CREDIT)
                .build();
        log.info("Operation check request: {}", operationCheckRequest);

        var operationCheckResponse = checkOperation(blockerServiceClient, operationCheckRequest);
        log.info("Operation check response: {}", operationCheckResponse);
        if (!operationCheckResponse.isApproved()) {
            throw new SuspiciousOperationException();
        }

        var debitBalanceRequest = DebitBalanceRequest.builder()
                .login(request.getLogin())
                .currency(request.getFromCurrency())
                .amount(debitAmount)
                .build();
        log.info("Debit balance request: {}", debitBalanceRequest);
        var debitBalanceResponse = debitBalance(accountBalanceServiceClient, debitBalanceRequest);
        log.info("Debit balance response: {}", debitBalanceResponse);


        var creditBalanceRequest = CreditBalanceRequest.builder()
                .login(request.getLogin())
                .currency(request.getToCurrency())
                .amount(request.getAmount())
                .build();
        log.info("Credit balance request: {}", creditBalanceRequest);
        var creditBalanceResponse = creditBalance(accountBalanceServiceClient, creditBalanceRequest);
        log.info("Credit balance response: {}", creditBalanceResponse);


        var approveOperationRequest = new ApproveOperationRequest(List.of(request.getLogin()));
        log.info("Approve operation request: {}", approveOperationRequest);
        var approveOperationResponse = approveBalance(accountBalanceServiceClient, approveOperationRequest);
        log.info("Approve operation response: {}", approveOperationResponse);

        var savedTransferOperation = transferOperationRepository.save(TransferOperation.builder()
                .fromLogin(request.getLogin())
                .fromCurrency(request.getFromCurrency())
                .fromExchangeRateToRub(exchangeRateToRub)
                .toLogin(request.getLogin())
                .toCurrency(request.getToCurrency())
                .amount(request.getAmount())
                .convertedAmount(debitAmount)
                .operationType(INTERNAL_TRANSFER)
                .build());
        log.info("Saved transfer operation: {}", savedTransferOperation);

        sendNotification(notificationServiceClient,
                new NotificationRequest(TRANSFER, BALANCE_TRANSFERRED_INTERNAL, request.getLogin(),
                        "Перевод на свой счёт выполнен"));
    }

    @Override
    public void externalTransfer(ExternalTransferRequest request) {
        log.info("External transfer request: {}", request);

        BigDecimal debitAmount = request.getAmount();
        Double exchangeRateToRub = null;

        if (!request.getFromCurrency().equals(request.getToCurrency())) {
            log.info("From currency and To currency are not the same");
            var conversionRequest = ConversionRequest.builder()
                    .amount(request.getAmount())
                    .sourceCurrency(request.getFromCurrency())
                    .targetCurrency(request.getToCurrency())
                    .build();
            log.info("Conversion request: {}", conversionRequest);

            var conversionResponse = convertAmount(exchangeServiceClient, conversionRequest);
            log.info("Convert response: {}", conversionResponse);

            debitAmount = conversionResponse.getConvertedAmount();
            exchangeRateToRub = conversionResponse.getExchangeRateToRub();
        }


        var operationCheckRequest = OperationCheckRequest.builder()
                .login(request.getFromLogin())
                .amount(debitAmount)
                .operationType(DEBIT)
                .build();
        log.info("Debit operation check request: {}", operationCheckRequest);

        var operationCheckResponse = checkOperation(blockerServiceClient, operationCheckRequest);
        log.info("Debit operation check response: {}", operationCheckResponse);
        if (!operationCheckResponse.isApproved()) {
            throw new SuspiciousOperationException();
        }

        operationCheckRequest = OperationCheckRequest.builder()
                .login(request.getToLogin())
                .amount(request.getAmount())
                .operationType(CREDIT)
                .build();
        log.info("Credit operation check request: {}", operationCheckRequest);

        operationCheckResponse = checkOperation(blockerServiceClient, operationCheckRequest);
        log.info("Credit operation check response: {}", operationCheckResponse);
        if (!operationCheckResponse.isApproved()) {
            throw new SuspiciousOperationException();
        }

        var debitBalanceRequest = DebitBalanceRequest.builder()
                .login(request.getFromLogin())
                .currency(request.getFromCurrency())
                .amount(debitAmount)
                .build();
        log.info("Debit balance request: {}", debitBalanceRequest);
        var debitBalanceResponse = debitBalance(accountBalanceServiceClient, debitBalanceRequest);
        log.info("Debit balance response: {}", debitBalanceResponse);

        var creditBalanceRequest = CreditBalanceRequest.builder()
                .login(request.getToLogin())
                .currency(request.getToCurrency())
                .amount(request.getAmount())
                .build();
        log.info("Credit balance request: {}", creditBalanceRequest);
        var creditBalanceResponse = creditBalance(accountBalanceServiceClient, creditBalanceRequest);
        log.info("Credit balance response: {}", creditBalanceResponse);

        var approveOperationRequest = new ApproveOperationRequest(List.of(request.getFromLogin(), request.getToLogin()));
        log.info("Approve operation request: {}", approveOperationRequest);
        var approveOperationResponse = approveBalance(accountBalanceServiceClient, approveOperationRequest);
        log.info("Approve operation response: {}", approveOperationResponse);

        var savedTransferOperation = transferOperationRepository.save(TransferOperation.builder()
                .fromLogin(request.getFromLogin())
                .fromCurrency(request.getFromCurrency())
                .fromExchangeRateToRub(exchangeRateToRub)
                .toLogin(request.getToLogin())
                .toCurrency(request.getToCurrency())
                .amount(request.getAmount())
                .convertedAmount(debitAmount)

                .operationType(EXTERNAL_TRANSFER)
                .build());

        log.info("Saved transfer operation: {}", savedTransferOperation);

        sendNotification(notificationServiceClient,
                new NotificationRequest(TRANSFER, BALANCE_TRANSFERRED_EXTERNAL, request.getFromLogin(), "Межсчетный перевод выполнен"));

        sendNotification(notificationServiceClient,
                new NotificationRequest(TRANSFER, BALANCE_TRANSFERRED_EXTERNAL, request.getToLogin(), "Межсчетный перевод выполнен"));
    }
}