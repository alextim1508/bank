package com.alextim.bank.account.service;

import com.alextim.bank.account.constant.Role;
import com.alextim.bank.account.entity.Account;
import com.alextim.bank.account.entity.Balance;
import com.alextim.bank.account.exception.AccountNotFoundException;
import com.alextim.bank.account.exception.BalanceNotOpenedException;
import com.alextim.bank.account.exception.InsufficientFundsException;
import com.alextim.bank.account.repository.AccountRepository;
import com.alextim.bank.account.repository.BalanceRepository;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.balance.ApproveOperationRequest;
import com.alextim.bank.common.dto.balance.CreditBalanceRequest;
import com.alextim.bank.common.dto.balance.DebitBalanceRequest;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {BalanceServiceImpl.class})
@ActiveProfiles("test")
class BalanceServiceTest {

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private BalanceRepository balanceRepository;

    @MockitoBean
    private NotificationServiceClient notificationServiceClient;

    @Autowired
    private BalanceServiceImpl balanceService;

    private Account account;
    private Balance balance;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .login("ivan_ivanov")
                .password("password")
                .firstName("ivan")
                .lastName("ivanov")
                .birthDate(LocalDate.of(1990, 8, 15))
                .roles(List.of(Role.USER))
                .build();

        balance = Balance.builder()
                .id(1L)
                .account(account)
                .currencyCode("RUB")
                .amount(new BigDecimal("1000"))
                .frozenAmount(BigDecimal.ZERO)
                .locked(false)
                .lockingTime(null)
                .opened(true)
                .build();

        account.setBalances(List.of(balance));


        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok( ApiResponse.success(new NotificationResponse("ivan_ivanov"))));
    }

    @Test
    void creditBalance_shouldIncreaseFrozenAmountTest() {
        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));

        balance.setFrozenAmount(new BigDecimal("100"));

        balanceService.creditBalance(new CreditBalanceRequest("ivan_ivanov", "RUB", new BigDecimal("200")));

        assertThat(balance.getFrozenAmount()).isEqualTo(new BigDecimal("300"));
        assertThat(balance.isLocked()).isTrue();
        assertThat(balance.getLockingTime()).isNotNull();
        verify(accountRepository).findByLogin("ivan_ivanov");
    }

    @Test
    void creditBalance_shouldThrowAccountNotFoundExceptionTest() {
        when(accountRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> balanceService.creditBalance(new CreditBalanceRequest("unknown", "RUB", BigDecimal.ONE)))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void creditBalance_shouldThrowBalanceClosedExceptionTest() {
        balance.setOpened(false);
        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> balanceService.creditBalance(new CreditBalanceRequest("ivan_ivanov", "RUB", BigDecimal.ONE)))
                .isInstanceOf(BalanceNotOpenedException.class);
    }

    @Test
    void debitBalance_shouldDecreaseFrozenAmountTest() {
        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));

        balanceService.debitBalance(new DebitBalanceRequest("ivan_ivanov", "RUB", new BigDecimal("300")));

        assertThat(balance.getFrozenAmount()).isEqualTo(new BigDecimal("-300"));
        assertThat(balance.isLocked()).isTrue();
        verify(accountRepository).findByLogin("ivan_ivanov");
    }

    @Test
    void debitBalance_shouldThrowInsufficientFundsExceptionTest() {
        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));

        balance.setFrozenAmount(new BigDecimal("1000"));

        assertThatExceptionOfType(InsufficientFundsException.class)
                .isThrownBy(() -> balanceService.debitBalance(
                        new DebitBalanceRequest("ivan_ivanov", "RUB", new BigDecimal("1500"))
                ))
                .satisfies(ex -> {
                    assertThat(ex.getMessage()).isEqualTo("Insufficient funds on the balance with login ivan_ivanov (current balance: 1000 RUB) to debit 1500");
                    assertThat(ex.getLogin()).isEqualTo("ivan_ivanov");
                    assertThat(ex.getCurrency()).isEqualTo("RUB");
                    assertThat(ex.getAccountAmount()).isEqualByComparingTo("1000");
                    assertThat(ex.getDebitAmount()).isEqualByComparingTo("1500");
                });
    }


    @Test
    void approve_shouldUnfreezeAllBalancesTest() {
        balance.setFrozenAmount(new BigDecimal("400"));
        balance.setLocked(true);
        balance.setLockingTime(LocalDateTime.now().minusSeconds(10));

        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        balanceService.approve(new ApproveOperationRequest(List.of("ivan_ivanov")));

        assertThat(balance.getAmount()).isEqualTo(new BigDecimal("1400"));
        assertThat(balance.getFrozenAmount()).isZero();
        assertThat(balance.isLocked()).isFalse();

        verify(balanceRepository, times(1)).save(balance);
    }

    @Test
    void unfreezeAccountBalance_shouldSendNotificationTest() {
        balance.setFrozenAmount(new BigDecimal("250"));
        balance.setLocked(true);
        balance.setLockingTime(LocalDateTime.now().minusSeconds(10));

        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);

        balanceService.unfreezeAccountBalance("ivan_ivanov");

        verify(notificationServiceClient).sendNotification(captor.capture());
        NotificationRequest notification = captor.getValue();
        assertThat(notification.getLogin()).isEqualTo("ivan_ivanov");
        assertThat(notification.getMessage()).contains("Balance updated");
    }
}