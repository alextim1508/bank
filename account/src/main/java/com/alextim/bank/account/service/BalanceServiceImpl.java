package com.alextim.bank.account.service;

import com.alextim.bank.account.entity.Account;
import com.alextim.bank.account.entity.Balance;
import com.alextim.bank.account.exception.*;
import com.alextim.bank.account.repository.AccountRepository;
import com.alextim.bank.account.repository.BalanceRepository;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.dto.balance.ApproveOperationRequest;
import com.alextim.bank.common.dto.balance.CreditBalanceRequest;
import com.alextim.bank.common.dto.balance.DebitBalanceRequest;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.alextim.bank.common.client.util.NotificationClientUtils.sendNotification;
import static com.alextim.bank.common.constant.AggregateType.ACCOUNT;
import static com.alextim.bank.common.constant.EventType.ACCOUNT_BALANCE_UPDATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceServiceImpl implements BalanceService {

    private final AccountRepository accountRepository;

    private final BalanceRepository balanceRepository;

    private final NotificationServiceClient notificationServiceClient;

    private final BalanceMetricsService balanceMetricsService;

    @Value("${account.balance.lock-timeout-seconds}")
    private int lockTimeoutSeconds;

    @Override
    public void creditBalance(CreditBalanceRequest request) {
        log.info("Credit balance. Request: {}", request);

        Balance balance = getValidatedBalance(request.getLogin(), request.getCurrency());
        log.info("Found balance: {}", balance);

        BigDecimal newFrozenAmount = balance.getFrozenAmount().add(request.getAmount());
        log.info("CREDIT: frozenAmount {} + {} = {}", balance.getFrozenAmount(), request.getAmount(), newFrozenAmount);

        frozenAmount(balance, newFrozenAmount);

        balanceRepository.save(balance);

        balanceMetricsService.incrementBalanceOperation("credit", "success");

        log.info("Successfully credited and frozen amount for login={}, currency={}", request.getLogin(), request.getCurrency());
    }

    @Override
    public void debitBalance(DebitBalanceRequest request) {
        log.info("Debit balance. Request: {}", request);

        Balance balance = getValidatedBalance(request.getLogin(), request.getCurrency());
        log.info("Found balance: {}", balance);

        if (balance.getAmount().compareTo(request.getAmount()) < 0) {
            balanceMetricsService.incrementBalanceOperation("debit", "failure", "insufficient_funds");

            throw new InsufficientFundsException(
                    request.getLogin(), request.getCurrency(),
                    balance.getAmount(), request.getAmount());
        }

        BigDecimal newFrozenAmount = balance.getFrozenAmount().subtract(request.getAmount());
        log.info("DEBIT: frozenAmount {} - {} = {}", balance.getFrozenAmount(), request.getAmount(), newFrozenAmount);

        frozenAmount(balance, newFrozenAmount);

        balanceRepository.save(balance);

        balanceMetricsService.incrementBalanceOperation("debit", "success");

        log.info("Successfully debited and updated frozen amount for login={}, currency={}", request.getLogin(), request.getCurrency());
    }

    private Balance getValidatedBalance(String login, String currency) {
        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));
        log.debug("Found account: login={}", login);

        if (account.isBlocked()) {
            throw new AccountLockedException(login, currency);
        }

        Balance accountBalance = account.getBalances().stream()
                .filter(balance -> balance.getCurrencyCode().equals(currency))
                .findAny()
                .orElseThrow(() -> new BalanceNotFoundException(login, currency));
        log.info("Found balance with currency {}", currency);

        if (!accountBalance.isOpened()) {
            throw new BalanceNotOpenedException(login, currency);
        }

        return accountBalance;
    }

    private void frozenAmount(Balance accountBalance, BigDecimal frozenAmount) {
        accountBalance.setFrozenAmount(frozenAmount);
        accountBalance.setLockingTime(LocalDateTime.now());
        accountBalance.setLocked(true);
        log.debug("Balance frozen: {}", accountBalance);
    }

    @Override
    public void approve(ApproveOperationRequest request) {
        log.info("Approve. Request: {}", request);
        request.getLogins().forEach(this::unfreezeAccountBalance);

        balanceMetricsService.incrementBalanceOperation("approve", "success");
    }

    public void unfreezeAccountBalance(String login) {
        log.info("Unfreezing balance for login: {}", login);

        Account account = accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));
        log.info("Found account with login {}", login);

        List<Balance> lockedBalances = account.getBalances().stream()
                .filter(Balance::isLocked)
                .toList();

        if (lockedBalances.isEmpty()) {
            log.info("No locked balances for login: {}", login);
            return;
        }

        for (Balance balance : lockedBalances) {
            log.info("balance: {}", balance);

            BigDecimal newAmount = balance.getAmount().add(balance.getFrozenAmount());
            log.info("Unfreezing: amount {} + frozen {} = {}", balance.getAmount(), balance.getFrozenAmount(), newAmount);

            balance.setAmount(newAmount);
            balance.setFrozenAmount(BigDecimal.ZERO);
            balance.setLocked(false);

            balanceRepository.save(balance);

            sendNotification(notificationServiceClient,
                    new NotificationRequest(ACCOUNT, ACCOUNT_BALANCE_UPDATED, account.getLogin(),
                            "Balance updated " + balance.getAmount()));
        }

        log.info("Successfully unfrozen {} balances for login: {}", lockedBalances.size(), login);
    }

    @Scheduled(fixedRate = 5000)
    public void clearFrozenAmount() {
        LocalDateTime timeLimit = LocalDateTime.now().minusSeconds(lockTimeoutSeconds);
        log.info("Scheduled task: clearing frozen amounts for balances locked before {}", timeLimit);

        int unlockedCount = balanceRepository.unlockAllBefore(timeLimit);

        if (unlockedCount == 0) {
            log.info("No expired locked balances found");
            return;
        }

        log.info("Auto-unlocked {} balances due to timeout", unlockedCount);
    }
}
