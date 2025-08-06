package com.alextim.bank.account.controller;

import com.alextim.bank.account.entity.Account;
import com.alextim.bank.account.entity.Balance;
import com.alextim.bank.account.repository.AccountRepository;
import com.alextim.bank.account.service.AccountService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class AccountControllerTest extends AbstractControllerTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

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
    private OAuth2TokenClient oauth2TokenClient;

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
    }

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    void createAccount_ShouldReturnCreatedAccount() throws Exception {
        mockMvc.perform(post("/account")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "password": "pass123",
                                    "firstName": "ivan",
                                    "lastName": "ivanov",
                                    "email": "ivan@ya.ru",
                                    "birthDate": "2000-12-21",
                                    "telegram" : "ivanTG"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.data.login").value("ivan_ivanov"))
                .andExpect(jsonPath("$.data.name").value("ivan ivanov"))
                .andExpect(jsonPath("$.data.birthDate").value("2000-12-21"))
                .andExpect(jsonPath("$.data.balances").isArray())
                .andExpect(jsonPath("$.data.balances").value(hasSize(2)))
                .andExpect(jsonPath("$.data.balances[*].code").value(hasItems("USD", "RUB")))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'USD')].title").value("US Dollar"))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'USD')].opened").value(true))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'USD')].amount").value(0))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'RUB')].title").value("Russian Ruble"))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'RUB')].opened").value(true))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'RUB')].amount").value(0));
    }

    @Test
    void updateAccount_ShouldReturnCreatedAccount() throws Exception {
        Account account = Account.builder()
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

        mockMvc.perform(put("/account")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "login": "ivan_ivanov",
                                    "name": "ivan ivanov",
                                    "birthDate": "2000-12-01",
                                    "currencyCodes": ["RUB"]
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.data.login").value("ivan_ivanov"))
                .andExpect(jsonPath("$.data.name").value("ivan ivanov"))
                .andExpect(jsonPath("$.data.birthDate").value("2000-12-01"))
                .andExpect(jsonPath("$.data.balances").isArray())
                .andExpect(jsonPath("$.data.balances").value(hasSize(2)))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'USD')].title").value("US Dollar"))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'USD')].opened").value(false))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'USD')].amount").value(1000))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'RUB')].title").value("Russian Ruble"))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'RUB')].opened").value(true))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'RUB')].amount").value(2000));
    }

    @Test
    void getAllAccounts_ShouldReturnListOfAccounts() throws Exception {
        Account account = Account.builder()
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

        account = Account.builder()
                .login("kirill_kirillov")
                .password("password")
                .firstName("Kirill")
                .lastName("Kirillov")
                .birthDate(LocalDate.of(2000, 12, 1))
                .build();
        account.setBalances(List.of(
                        Balance.builder().amount(BigDecimal.valueOf(1000)).currencyCode("USD").account(account).build(),
                        Balance.builder().amount(BigDecimal.valueOf(2000)).currencyCode("RUB").account(account).build()
                )
        );

        accountRepository.save(account);

        mockMvc.perform(get("/account")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.data[?(@.login == 'ivan_ivanov')].name").value("Ivan Ivanov"))
                .andExpect(jsonPath("$.data[?(@.login == 'kirill_kirillov')].name").value("Kirill Kirillov"));
    }

    @Test
    void getAccount_ShouldReturnAccountByLogin() throws Exception {
        Account account = Account.builder()
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

        mockMvc.perform(get("/account/search")
                        .with(jwt())
                        .param("login", "ivan_ivanov"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.error").isEmpty())
                .andExpect(jsonPath("$.data.login").value("ivan_ivanov"))
                .andExpect(jsonPath("$.data.name").value("Ivan Ivanov"))
                .andExpect(jsonPath("$.data.birthDate").value("2000-12-01"))
                .andExpect(jsonPath("$.data.balances").isArray())
                .andExpect(jsonPath("$.data.balances").value(hasSize(2)))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'USD')].title").value("US Dollar"))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'USD')].opened").value(true))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'USD')].amount").value(1000))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'RUB')].title").value("Russian Ruble"))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'RUB')].opened").value(true))
                .andExpect(jsonPath("$.data.balances[?(@.code == 'RUB')].amount").value(2000));
    }

    @Test
    void handleAccountNotFoundException_ShouldReturnAccountNotFound() throws Exception {
        mockMvc.perform(get("/account/search")
                        .with(jwt())
                        .param("login", "ivan_ivanov"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Account not found"))
                .andExpect(jsonPath("$.error.details").value("Account with login ivan_ivanov not found"));
    }

    @Test
    void handleValidationException_ShouldReturnValidationFailed_WhenInvalidLogin() throws Exception {
        mockMvc.perform(post("/account")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {
                                    "login": "123",
                                    "password": "pass123",
                                    "firstName": "ivan",
                                    "lastName": "ivanov",
                                    "email": "ivan@ya.ru",
                                    "birthDate": "2000-12-21",
                                    "telegram" : "ivanTG"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details").value("{login=[Login must be between 4 and 20 characters]}"));
    }

    @Test
    void handleValidationException_ShouldReturnValidationFailedWithOnRussianDetails_WhenInvalidLogin() throws Exception {
        mockMvc.perform(post("/account")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "ru")
                        .content("""
                                 {
                                    "login": "123",
                                    "password": "pass123",
                                    "firstName": "ivan",
                                    "lastName": "ivanov",
                                    "email": "ivan@ya.ru",
                                    "birthDate": "2000-12-21",
                                    "telegram" : "ivanTG"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details").value("{login=[Логин должен быть от 4 до 20 символов]}"));
    }

    @Test
    void handleValidationException_ShouldReturnValidationFailed_WhenInvalidBirthDate() throws Exception {
        mockMvc.perform(post("/account")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {
                                    "login": "ivan_ivanov",
                                    "password": "pass123",
                                    "firstName": "ivan",
                                    "lastName": "ivanov",
                                    "email": "ivan@ya.ru",
                                    "birthDate": "2020-12-21",
                                    "telegram" : "ivanTG"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details").value("{birthDate=[You must be at least 18 years old]}"));
    }
}