package com.alextim.bank.cache.service;

import com.alextim.bank.cache.exception.SuspiciousOperationException;
import com.alextim.bank.cache.repository.CashRepository;
import com.alextim.bank.common.client.AccountBalanceServiceClient;
import com.alextim.bank.common.client.BlockerServiceClient;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.balance.*;
import com.alextim.bank.common.dto.blocker.OperationCheckRequest;
import com.alextim.bank.common.dto.blocker.OperationCheckResponse;
import com.alextim.bank.common.dto.cash.DepositRequest;
import com.alextim.bank.common.dto.cash.WithdrawRequest;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import com.alextim.bank.common.exception.AccountBalanceServiceClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static com.alextim.bank.cache.constant.CashOperationType.DEPOSIT;
import static com.alextim.bank.cache.constant.CashOperationType.WITHDRAW;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {CashServiceImpl.class})
@ActiveProfiles("test")
class CashServiceImplTest {

    @Autowired
    private CashServiceImpl cashService;

    @MockitoBean
    private AccountBalanceServiceClient accountBalanceServiceClient;

    @MockitoBean
    private BlockerServiceClient blockerServiceClient;

    @MockitoBean
    private NotificationServiceClient notificationServiceClient;

    @MockitoBean
    private CashRepository cashRepository;

    @MockitoBean
    private ATMService atmService;

    private DepositRequest depositRequest;
    private WithdrawRequest withdrawRequest;

    @BeforeEach
    void setUp() {
        depositRequest = DepositRequest.builder()
                .login("ivan")
                .currency("RUB")
                .amount(new BigDecimal("1000"))
                .build();

        withdrawRequest = WithdrawRequest.builder()
                .login("ivan")
                .currency("RUB")
                .amount(new BigDecimal("1000"))
                .build();

        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new NotificationResponse("ivan"))));

    }

    @Test
    void deposit_ShouldSucceed_WhenOperationApproved() {
        when(blockerServiceClient.checkOperation(any(OperationCheckRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new OperationCheckResponse("ivan", true))));

        when(accountBalanceServiceClient.creditBalance(any(CreditBalanceRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new CreditBalanceResponse("ivan"))));


        when(accountBalanceServiceClient.approve(any(ApproveOperationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new ApproveOperationResponse(List.of("ivan")))));

        cashService.deposit(depositRequest);

        verify(atmService).depositCash(new BigDecimal("1000"));

        verify(cashRepository).save(argThat(op ->
                op.getLogin().equals("ivan") &&
                        op.getCurrency().equals("RUB") &&
                        op.getAmount().compareTo(new BigDecimal("1000")) == 0 &&
                        op.getOperationType() == DEPOSIT
        ));
    }

    @Test
    void deposit_ShouldThrowSuspiciousOperationException_WhenBlocked() {
        when(blockerServiceClient.checkOperation(any(OperationCheckRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new OperationCheckResponse("ivan", false))));

        assertThatThrownBy(() -> cashService.deposit(depositRequest))
                .isInstanceOf(SuspiciousOperationException.class);
    }

    @Test
    void withdraw_ShouldSucceed_WhenOperationApproved() {
        when(blockerServiceClient.checkOperation(any(OperationCheckRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new OperationCheckResponse("ivan", true))));

        when(accountBalanceServiceClient.debitBalance(any(DebitBalanceRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new DebitBalanceResponse("ivan"))));

        when(accountBalanceServiceClient.approve(any(ApproveOperationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new ApproveOperationResponse(List.of("ivan")))));


        doNothing().when(atmService).withdrawCash(any());

        cashService.withdraw(withdrawRequest);

        verify(cashRepository).save(argThat(op ->
                op.getLogin().equals("ivan") &&
                op.getCurrency().equals("RUB") &&
                op.getAmount().compareTo(new BigDecimal("1000")) == 0 &&
                op.getOperationType() == WITHDRAW
        ));
    }

    @Test
    void withdraw_ShouldThrowException_WhenDebitFails() {
        when(blockerServiceClient.checkOperation(any(OperationCheckRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new OperationCheckResponse("ivan", true))));

        when(accountBalanceServiceClient.debitBalance(any(DebitBalanceRequest.class)))
                .thenReturn(ResponseEntity.badRequest().body(ApiResponse.error("Insufficient funds", "")));

        assertThatThrownBy(() -> cashService.withdraw(withdrawRequest))
                .isInstanceOf(AccountBalanceServiceClientException.class);
    }
}