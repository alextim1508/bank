package com.alextim.bank.front.controller;

import com.alextim.bank.common.client.AccountServiceClient;
import com.alextim.bank.common.client.ExchangeServiceClient;
import com.alextim.bank.common.dto.account.AccountFullResponse;
import com.alextim.bank.common.dto.account.AccountResponse;
import com.alextim.bank.common.dto.exchange.CurrencyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

import static com.alextim.bank.common.client.util.AccountClientUtils.getAccountByLogin;
import static com.alextim.bank.common.client.util.AccountClientUtils.getAccounts;
import static com.alextim.bank.common.client.util.ExchangeClientUtils.getCurrencies;

@Controller
@RequestMapping("/front/main")
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final AccountServiceClient accountServiceClient;

    private final ExchangeServiceClient exchangeServiceClient;

    @GetMapping
    public String mainPage(@RequestHeader(value = "${jwt.login-header-name}") String login,
                           @RequestParam(required = false) List<String> userAccountsErrors,
                           @RequestParam(required = false) List<String> passwordErrors,
                           @RequestParam(required = false) List<String> cashErrors,
                           @RequestParam(required = false) List<String> transferErrors,
                           @RequestParam(required = false) List<String> transferOtherErrors,
                           Model model
                            /*RedirectAttributes redirectAttributes*/) {
        log.info("Incoming request for getting main page. Login: {}", login);

        AccountFullResponse currentAccount = null;
        try {
            currentAccount = getAccountByLogin(accountServiceClient, login);
        } catch (RuntimeException e) {
            log.error("getAccountByLoginError", e);

            if (userAccountsErrors == null) {
                userAccountsErrors = new ArrayList<>();
            }
            userAccountsErrors.add(e.getMessage());
        }

        List<AccountResponse> accounts = null;
        try {
            accounts = getAccounts(accountServiceClient);
        } catch (RuntimeException e) {
            if (userAccountsErrors == null) {
                userAccountsErrors = new ArrayList<>();
            }
            userAccountsErrors.add(e.getMessage());
        }

        List<CurrencyResponse> currencies = null;
        try {
            currencies = getCurrencies(exchangeServiceClient);
        } catch (RuntimeException e) {
            if (transferOtherErrors == null) {
                transferOtherErrors = new ArrayList<>();
            }
            transferOtherErrors.add(e.getMessage());
        }


        model.addAttribute("login", currentAccount != null ? currentAccount.getLogin() : "-");
        model.addAttribute("name", currentAccount != null ? currentAccount.getName() : "-");
        model.addAttribute("birthdate", currentAccount != null ? currentAccount.getBirthDate() : "-");
        model.addAttribute("balances", currentAccount != null ? currentAccount.getBalances() : "-");

        model.addAttribute("users", accounts);

        model.addAttribute("currency", currencies);

        model.addAttribute("userAccountsErrors", userAccountsErrors);
        model.addAttribute("passwordErrors", passwordErrors);
        model.addAttribute("cashErrors", cashErrors);
        model.addAttribute("transferErrors", transferErrors);
        model.addAttribute("transferOtherErrors", transferOtherErrors);

        return "main";
    }
}
