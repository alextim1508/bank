package com.alextim.bank.transfer.service;

import com.alextim.bank.common.client.AccountBalanceServiceClient;
import com.alextim.bank.common.client.BlockerServiceClient;
import com.alextim.bank.common.client.ExchangeServiceClient;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.balance.*;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import com.alextim.bank.common.dto.blocker.OperationCheckResponse;
import com.alextim.bank.common.dto.exchange.ConversionRequest;
import com.alextim.bank.common.dto.exchange.ConversionResponse;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import com.alextim.bank.common.dto.transfer.ExternalTransferRequest;
import com.alextim.bank.common.dto.transfer.InternalTransferRequest;
import com.alextim.bank.transfer.exception.SuspiciousOperationException;
import com.alextim.bank.transfer.repository.TransferOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static com.alextim.bank.transfer.constant.TransferOperationType.EXTERNAL_TRANSFER;
import static com.alextim.bank.transfer.constant.TransferOperationType.INTERNAL_TRANSFER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {TransferServiceImpl.class})
@ActiveProfiles("test")
class TransferServiceImplTest {

    @Autowired
    private TransferServiceImpl transferService;

    @MockitoBean
    private AccountBalanceServiceClient accountBalanceServiceClient;

    @MockitoBean
    private ExchangeServiceClient exchangeServiceClient;

    @MockitoBean
    private BlockerServiceClient blockerServiceClient;

    @MockitoBean
    private NotificationServiceClient notificationServiceClient;

    @MockitoBean
    private TransferOperationRepository transferOperationRepository;


    private InternalTransferRequest internalRequest;
    private ExternalTransferRequest externalRequest;


    @Test
    void internalTransfer_ShouldTransferSuccessfully_WhenSameCurrency() {
        internalRequest = InternalTransferRequest.builder()
                .login("ivan")
                .fromCurrency("RUB")
                .toCurrency("RUB")
                .amount(new BigDecimal("100"))
                .build();

        when(blockerServiceClient.checkOperation(any(OperationCheckRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new OperationCheckResponse("ivan", true))));
        when(accountBalanceServiceClient.debitBalance(any(DebitBalanceRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new DebitBalanceResponse("ivan"))));
        when(accountBalanceServiceClient.creditBalance(any(CreditBalanceRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new CreditBalanceResponse("ivan"))));
        when(accountBalanceServiceClient.approve(any(ApproveOperationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new ApproveOperationResponse(List.of("ivan")))));
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new NotificationResponse("ivan"))));

        transferService.internalTransfer(internalRequest);

        verify(transferOperationRepository).save(argThat(op ->
                op.getFromLogin().equals("ivan") &&
                        op.getToLogin().equals("ivan") &&
                        op.getOperationType() == INTERNAL_TRANSFER &&
                        op.getAmount().compareTo(new BigDecimal("100")) == 0 &&
                        op.getConvertedAmount().compareTo(new BigDecimal("100")) == 0 &&
                        op.getFromExchangeRateToRub() == null
        ));
    }


    @Test
    void internalTransfer_ShouldConvertAndTransfer_WhenDifferentCurrencies() {
        internalRequest = InternalTransferRequest.builder()
                .login("ivan")
                .fromCurrency("RUB")
                .toCurrency("USD")
                .amount(new BigDecimal("100"))
                .build();

        when(exchangeServiceClient.convert(any(ConversionRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(ConversionResponse.builder()
                        .sourceCurrency("RUB")
                        .targetCurrency("USD")
                        .amount(new BigDecimal("100"))
                        .convertedAmount(new BigDecimal("1.11"))
                        .exchangeRateToRub(90.0)
                        .build())));

        when(blockerServiceClient.checkOperation(any(OperationCheckRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new OperationCheckResponse("ivan", true))));
        when(accountBalanceServiceClient.debitBalance(any(DebitBalanceRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new DebitBalanceResponse("ivan"))));
        when(accountBalanceServiceClient.creditBalance(any(CreditBalanceRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new CreditBalanceResponse("ivan"))));
        when(accountBalanceServiceClient.approve(any(ApproveOperationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new ApproveOperationResponse(List.of("ivan")))));
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new NotificationResponse("ivan"))));

        transferService.internalTransfer(internalRequest);

        verify(transferOperationRepository).save(argThat(op ->
                        op.getFromCurrency().equals("RUB") &&
                                op.getToCurrency().equals("USD") &&
                                op.getFromLogin().equals("ivan") &&
                                op.getToLogin().equals("ivan") &&
                                op.getOperationType() == INTERNAL_TRANSFER  &&
                op.getAmount().compareTo(new BigDecimal("100")) == 0 &&
                        op.getConvertedAmount().compareTo(new BigDecimal("1.11")) == 0 &&
                        op.getFromExchangeRateToRub().equals(90.0)
        ));
    }

    @Test
    void externalTransfer_ShouldTransferSuccessfully() {
        externalRequest = ExternalTransferRequest.builder()
                .fromLogin("ivan")
                .toLogin("petr")
                .fromCurrency("RUB")
                .toCurrency("USD")
                .amount(new BigDecimal("100"))
                .build();

        when(exchangeServiceClient.convert(any(ConversionRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(ConversionResponse.builder()
                        .sourceCurrency("RUB")
                        .targetCurrency("USD")
                        .amount(new BigDecimal("100"))
                        .convertedAmount(new BigDecimal("1.11"))
                        .exchangeRateToRub(90.0)
                        .build())));

        when(blockerServiceClient.checkOperation(any(OperationCheckRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new OperationCheckResponse("ivan", true))));
        when(accountBalanceServiceClient.debitBalance(any(DebitBalanceRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new DebitBalanceResponse("ivan"))));
        when(accountBalanceServiceClient.creditBalance(any(CreditBalanceRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new CreditBalanceResponse("ivan"))));
        when(accountBalanceServiceClient.approve(any(ApproveOperationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new ApproveOperationResponse(List.of("ivan")))));
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new NotificationResponse("ivan"))));

        transferService.externalTransfer(externalRequest);


        verify(transferOperationRepository).save(argThat(op ->
                op.getFromCurrency().equals("RUB") &&
                        op.getToCurrency().equals("USD") &&
                        op.getFromLogin().equals("ivan") &&
                        op.getToLogin().equals("petr") &&
                        op.getOperationType() == EXTERNAL_TRANSFER
                        && op.getAmount().compareTo(new BigDecimal("100")) == 0 &&
                       op.getConvertedAmount().compareTo(new BigDecimal("1.11")) == 0 &&
                op.getFromExchangeRateToRub().equals(90.0)
        ));
    }


    @Test
    void internalTransfer_ShouldThrowSuspiciousOperationException_WhenBlocked() {
        internalRequest = InternalTransferRequest.builder()
                .login("ivan")
                .fromCurrency("RUB")
                .toCurrency("USD")
                .amount(new BigDecimal("100"))
                .build();

        when(exchangeServiceClient.convert(any(ConversionRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(ConversionResponse.builder()
                        .sourceCurrency("RUB")
                        .targetCurrency("USD")
                        .amount(new BigDecimal("100"))
                        .convertedAmount(new BigDecimal("1.11"))
                        .exchangeRateToRub(90.0)
                        .build())));

        when(blockerServiceClient.checkOperation(any(OperationCheckRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new OperationCheckResponse("ivan", false))));

        assertThatThrownBy(() -> transferService.internalTransfer(internalRequest))
                .isInstanceOf(SuspiciousOperationException.class);
    }
}