package com.alextim.bank.front.controller;

import com.alextim.bank.common.client.AccountServiceClient;
import com.alextim.bank.common.client.CashServiceClient;
import com.alextim.bank.common.client.TransferServiceClient;
import com.alextim.bank.common.dto.account.AccountPasswordUpdateRequest;
import com.alextim.bank.common.dto.account.AccountUpdateRequest;
import com.alextim.bank.common.dto.cash.DepositRequest;
import com.alextim.bank.common.dto.cash.WithdrawRequest;
import com.alextim.bank.common.dto.transfer.ExternalTransferRequest;
import com.alextim.bank.common.dto.transfer.InternalTransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.alextim.bank.common.client.util.AccountClientUtils.updateAccount;
import static com.alextim.bank.common.client.util.AccountClientUtils.updateAccountPassword;
import static com.alextim.bank.common.client.util.CashClientUnits.deposit;
import static com.alextim.bank.common.client.util.CashClientUnits.withdraw;
import static com.alextim.bank.common.client.util.TransferClientUnits.makeExternalTransfer;
import static com.alextim.bank.common.client.util.TransferClientUnits.makeInternalTransfer;


@Controller
@RequestMapping("/front/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountServiceClient accountServiceClient;

    private final TransferServiceClient transferServiceClient;

    private final CashServiceClient cashServiceClient;

    @PostMapping("/editPassword")
    public String editPassword(@RequestHeader(value = "${jwt.login-header-name}") String login,
                               @RequestParam String password,
                               @RequestParam String confirm_password) {
        log.info("Incoming request for editing password. Login: {}", login);

        if (!password.equals(confirm_password)) {
            return "redirect:/main?passwordErrors=" + "Пароли не совпадают";
        }

        try {
            updateAccountPassword(accountServiceClient, new AccountPasswordUpdateRequest(login, password));
        } catch (RuntimeException e) {
            return "redirect:/main?passwordErrors=" + e.getMessage();
        }

        return "redirect:/main";
    }

    @PostMapping("/editUserAccounts")
    public String editAccount(@RequestHeader(value = "${jwt.login-header-name}") String login,
                              @RequestParam String name,
                              @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate birthdate,
                              @RequestParam("balance") List<String> currencyCodes) {
        log.info("Incoming request for editing account. Login: {}", login);

        AccountUpdateRequest accountUpdateRequest = AccountUpdateRequest.builder()
                .login(login)
                .name(name)
                .birthDate(birthdate)
                .currencyCodes(currencyCodes)
                .build();

        try {
            updateAccount(accountServiceClient, accountUpdateRequest);
        } catch (RuntimeException e) {
            return "redirect:/main?userAccountsErrors=" + e.getMessage();
        }

        return "redirect:/main";
    }

    @PostMapping("/cash")
    public String cash(@RequestHeader(value = "${jwt.login-header-name}") String login,
                       @RequestParam String currency,
                       @RequestParam BigDecimal amount,
                       @RequestParam String action) {
        log.info("Incoming request for cash. Login: {}", login);

        if (action.equals("PUT")) {
            DepositRequest request = DepositRequest.builder()
                    .login(login)
                    .currency(currency)
                    .amount(amount)
                    .build();
            try {
                deposit(cashServiceClient, request);
            } catch (RuntimeException e) {
                log.error("deposit error", e);

                return "redirect:/main?cashErrors=" + List.of(e.getMessage());
            }
        } else if (action.equals("GET")) {
            WithdrawRequest request = WithdrawRequest.builder()
                    .login(login)
                    .currency(currency)
                    .amount(amount)
                    .build();
            try {
                withdraw(cashServiceClient, request);
            } catch (RuntimeException e) {
                log.error("withdraw error", e);

                return "redirect:/main?cashErrors=" + List.of(e.getMessage());
            }
        }

        System.out.println("cash OK");

        return "redirect:/main";
    }


    @PostMapping("/transfer")
    public String transfer(@RequestHeader(value = "${jwt.login-header-name}") String login,
                           @RequestParam("from_currency") String fromCurrency,
                           @RequestParam("to_currency") String toCurrency,
                           @RequestParam BigDecimal amount,
                           @RequestParam("to_login") String toLogin) {

        if (login.equals(toLogin)) {
            InternalTransferRequest request = InternalTransferRequest.builder()
                    .login(login)
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .amount(amount)
                    .build();

            try {
                makeInternalTransfer(transferServiceClient, request);
            } catch (RuntimeException e) {
                log.error("makeInternalTransfer error", e);

                return "redirect:/main?transferErrors=" + List.of(e.getMessage());
            }
        } else {
            ExternalTransferRequest request = ExternalTransferRequest.builder()
                    .fromLogin(login)
                    .toLogin(toLogin)
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .amount(amount)
                    .build();
            try {
                makeExternalTransfer(transferServiceClient, request);
            } catch (RuntimeException e) {
                log.error("makeInternalTransfer error", e);

                return "redirect:/main?transferErrors=" + List.of(e.getMessage());
            }
        }

        System.out.println("transfer OK");

        return "redirect:/main";
    }
}