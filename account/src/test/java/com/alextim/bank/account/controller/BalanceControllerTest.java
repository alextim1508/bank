package com.alextim.bank.account.controller;

import com.alextim.bank.account.entity.Account;
import com.alextim.bank.account.entity.Balance;
import com.alextim.bank.account.repository.AccountRepository;
import com.alextim.bank.account.service.BalanceService;
import com.alextim.bank.account.service.CurrencyService;
import com.alextim.bank.common.client.AuthServiceClient;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.client.OAuth2TokenClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.auth.TokenStatusResponse;
import com.alextim.bank.common.dto.exchange.CurrencyResponse;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class BalanceControllerTest extends AbstractControllerTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MessageSource messageSource;

    @MockitoBean
    private NotificationServiceClient notificationServiceClient;

    @MockitoBean
    private CurrencyService currencyService;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;
    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Account account;

    @BeforeEach
    public void setUp() {
        when(notificationServiceClient.sendNotification(any()))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new NotificationResponse("OK"))));

        when(authServiceClient.checkTokenStatus("valid-token"))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new TokenStatusResponse(true, false, false))));

        when(currencyService.getCurrencies()).thenReturn(List.of(
                new CurrencyResponse("US Dollar", "USD"),
                new CurrencyResponse("Russian Ruble", "RUB")
        ));

        account = Account.builder()
                .login("ivan_ivanov")
                .password("password")
                .firstName("Ivan")
                .lastName("Ivanov")
                .birthDate(LocalDate.of(2000, 12, 1))
                .build();
        account.setBalances(List.of(
                        Balance.builder().amount(BigDecimal.valueOf(1000)).currencyCode("USD").account(account).build(),
                        Balance.builder().amount(BigDecimal.valueOf(2000)).currencyCode("RUB").account(account).build()
                )
        );

        accountRepository.save(account);
    }

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
    }

    @Transactional
    @Test
    void debitBalance_ShouldReturnSuccess_WhenValidRequest() throws Exception {
        mockMvc.perform(post("/account/balance/debit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "currency": "USD",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.data.login").value("ivan_ivanov"));

        Account updatedAccount = accountRepository.findByLogin("ivan_ivanov").orElseThrow();
        Balance updatedUsdBalance = updatedAccount.getBalances().stream()
                .filter(b -> b.getCurrencyCode().equals("USD"))
                .findFirst()
                .orElseThrow();

        assertThat(updatedUsdBalance.getAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(1000));

        assertThat(updatedUsdBalance.getFrozenAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(-100));


        mockMvc.perform(post("/account/balance/approve")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "logins": ["ivan_ivanov"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.logins").value(hasItems("ivan_ivanov")));

        updatedAccount = accountRepository.findByLogin("ivan_ivanov").orElseThrow();
        updatedUsdBalance = updatedAccount.getBalances().stream()
                .filter(b -> b.getCurrencyCode().equals("USD"))
                .findFirst()
                .orElseThrow();

        assertThat(updatedUsdBalance.getAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(900));

        assertThat(updatedUsdBalance.getFrozenAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void debitBalance_ShouldReturnError_WhenAccountNotFound() throws Exception {
        mockMvc.perform(post("/account/balance/debit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "user123",
                                    "currency": "USD",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.message").value("Account not found"))
                .andExpect(jsonPath("$.error.details").value("Account with login user123 not found"));
    }

    @Test
    void debitBalance_ShouldReturnError_WhenBalanceNotOpenedException() throws Exception {
        Balance rub = account.getBalances().stream()
                .filter(balance -> balance.getCurrencyCode().equals("RUB"))
                .peek(balance -> balance.setOpened(false))
                .findFirst().orElseThrow();
        accountRepository.save(account);

        mockMvc.perform(post("/account/balance/debit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "currency": "RUB",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.message").value("Account not opened"))
                .andExpect(jsonPath("$.error.details").value("Balance RUB of Account with login ivan_ivanov is not opened"));
    }

    @Test
    void debitBalance_ShouldReturnError_WhenInsufficientFunds() throws Exception {
        mockMvc.perform(post("/account/balance/debit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "currency": "USD",
                                    "amount": 1200
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Insufficient funds"))
                .andExpect(jsonPath("$.error.details").value("Insufficient funds on the balance with login ivan_ivanov (current balance: 1000 USD) to debit 1200"));
    }

    @Transactional
    @Test
    void creditBalance_ShouldReturnSuccess_WhenValidRequest() throws Exception {
        mockMvc.perform(post("/account/balance/credit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "currency": "USD",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.login").value("ivan_ivanov"));

        Account updatedAccount = accountRepository.findByLogin("ivan_ivanov").orElseThrow();
        Balance updatedUsdBalance = updatedAccount.getBalances().stream()
                .filter(b -> b.getCurrencyCode().equals("USD"))
                .findFirst()
                .orElseThrow();

        assertThat(updatedUsdBalance.getAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(1000));

        assertThat(updatedUsdBalance.getFrozenAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(100));


        mockMvc.perform(post("/account/balance/approve")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "logins": ["ivan_ivanov"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.logins").value(hasItems("ivan_ivanov")));

        updatedAccount = accountRepository.findByLogin("ivan_ivanov").orElseThrow();
        updatedUsdBalance = updatedAccount.getBalances().stream()
                .filter(b -> b.getCurrencyCode().equals("USD"))
                .findFirst()
                .orElseThrow();

        assertThat(updatedUsdBalance.getAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(1100));

        assertThat(updatedUsdBalance.getFrozenAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void creditBalance_ShouldReturnError_WhenAccountNotFound() throws Exception {
        mockMvc.perform(post("/account/balance/credit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "login": "user123",
                                "currency": "USD",
                                "amount": 100
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Account not found"))
                .andExpect(jsonPath("$.error.details").value("Account with login user123 not found"));
    }

    @Test
    void creditBalance_ShouldReturnError_WhenBalanceNotFound() throws Exception {
        mockMvc.perform(post("/account/balance/credit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "login": "ivan_ivanov",
                                "currency": "ZAR",
                                "amount": 100
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Balance not found"))
                .andExpect(jsonPath("$.error.details").value("Balance in the currency ZAR not found of account with login ivan_ivanov"));

    }


    @Test
    void approve_ShouldReturnSuccess_WhenValidRequest() throws Exception {
        mockMvc.perform(post("/account/balance/approve")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "logins": ["user123"]
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.message").value("Account not found"))
                .andExpect(jsonPath("$.error.details").value("Account with login user123 not found"));
    }


    @Test
    void debitBalance_ShouldReturnValidationFailed_WhenLoginEmpty() throws Exception {
        mockMvc.perform(post("/account/balance/debit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {                            
                                "currency": "USD",
                                "amount": 100
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details").value("{login=[Login cannot be empty]}"));
    }

    @Test
    void creditBalance_ShouldReturnValidationFailed_WhenAmountZero() throws Exception {
        mockMvc.perform(post("/account/balance/credit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "login": "ivan_ivanov",
                                "currency": "USD",
                                "amount": 0
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details").value("{amount=[Amount must be greater than zero]}"));
    }

    @Test
    @WithMockUser
    void creditBalance_ShouldReturnValidationFailed_WhenCurrencyInvalid() throws Exception {
        mockMvc.perform(post("/account/balance/credit")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "login": "ivan_ivanov",
                                "currency": "US",
                                "amount": 100
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details").value("{currency=[Currency code must be 3 letters (e.g. USD, EUR)]}"));
    }
}