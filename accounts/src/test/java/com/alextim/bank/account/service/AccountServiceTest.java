package com.alextim.bank.account.service;

import com.alextim.bank.account.constant.ContactType;
import com.alextim.bank.account.constant.Role;
import com.alextim.bank.account.entity.Account;
import com.alextim.bank.account.entity.Balance;
import com.alextim.bank.account.entity.Contact;
import com.alextim.bank.account.exception.AccountNotFoundException;
import com.alextim.bank.account.mapper.AccountMapper;
import com.alextim.bank.account.repository.AccountRepository;
import com.alextim.bank.account.repository.ContactRepository;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.dto.ApiResponse;
import com.alextim.bank.common.dto.account.*;
import com.alextim.bank.common.dto.exchange.CurrencyResponse;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import com.alextim.bank.common.dto.notification.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.alextim.bank.account.constant.ContactType.EMAIL;
import static com.alextim.bank.account.constant.ContactType.TELEGRAM;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {AccountServiceImpl.class})
@ActiveProfiles("test")
class AccountServiceTest {

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private ContactRepository contactRepository;

    @MockitoBean
    private AccountMapper accountMapper;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CurrencyService currencyService;

    @MockitoBean
    private NotificationServiceClient notificationServiceClient;

    @Autowired
    private AccountServiceImpl accountService;

    private Account account;
    private List<Balance> balances;
    private List<Contact> contacts;

    private AccountRequest accountRequest;
    private AccountFullResponse accountFullResponse;
    private List<CurrencyResponse> currencyResponses;
    private List<AccountBalanceResponse> accountBalanceResponse;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .login("ivan_ivanov")
                .password("encoded_password")
                .firstName("ivan")
                .lastName("ivanov")
                .birthDate(LocalDate.of(1990, 1, 1))
                .roles(List.of(Role.USER))
                .build();

        balances = Arrays.asList(
                Balance.builder().currencyCode("RUB").account(account).build(),
                Balance.builder().currencyCode("USD").account(account).build(),
                Balance.builder().currencyCode("EUR").account(account).build()
        );
        account.setBalances(balances);

        contacts = Arrays.asList(
                new Contact(EMAIL, "ivan@example.com", account),
                new Contact(TELEGRAM, "@ivan", account)
        );
        account.setContacts(contacts);


        accountRequest = new AccountRequest(
                "ivan_ivanov",
                "password",
                "ivan",
                "ivanov",
                LocalDate.of(1990, 1, 1),
                "ivan@example.com",
                "@ivan");

        currencyResponses = Arrays.asList(
                new CurrencyResponse("Рубль", "RUB"),
                new CurrencyResponse("Доллар", "USD"),
                new CurrencyResponse("Евро", "EUR")
        );

        accountBalanceResponse = currencyResponses.stream()
                .map(currencyResponse ->
                        new AccountBalanceResponse(currencyResponse.getCode(), currencyResponse.getTitle(), true, new BigDecimal(10))
                )
                .toList();

        accountFullResponse = new AccountFullResponse(
                "ivan_ivanov",
                "ivan ivanov",
                LocalDate.of(1990, 1, 1),
                accountBalanceResponse
        );

        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(new NotificationResponse("ivan_ivanov"))));
    }

    @Test
    void createAccount_shouldReturnFullResponse_WhenValidRequest() {
        when(accountMapper.toEntity(any(AccountRequest.class))).thenReturn(account);

        when(currencyService.getCurrencies()).thenReturn(currencyResponses);

        when(contactRepository.existsByTypeAndValue(any(ContactType.class), anyString())).thenReturn(false);

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(accountMapper.toAccountBalanceResponses(anyList(), anyList()))
                .thenReturn(accountBalanceResponse);

        when(accountMapper.toFullDto(any(Account.class), anyList()))
                .thenReturn(accountFullResponse);

        AccountFullResponse response = accountService.createAccount(accountRequest);

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(accountFullResponse);

        verify(accountRepository).save(any(Account.class));
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));

    }

    @Test
    void updateAccount_ShouldUpdateFields_WhenValidRequest() {
        AccountUpdateRequest request = new AccountUpdateRequest(
                "ivan_ivanov",
                "ivan ivanov",
                LocalDate.of(1990, 1, 1),
                List.of("RUB", "USD")
        );

        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));
        when(currencyService.getCurrencies()).thenReturn(currencyResponses);
        when(accountMapper.toAccountBalanceResponses(currencyResponses, account.getBalances()))
                .thenReturn(accountBalanceResponse);
        when(accountMapper.toFullDto(any(Account.class), anyList()))
                .thenReturn(accountFullResponse);

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));


        AccountFullResponse response = accountService.updateAccount(request);

        assertThat(response).isNotNull();
        assertThat(account.getFirstName()).isEqualTo("ivan");
        assertThat(account.getLastName()).isEqualTo("ivanov");
        assertThat(account.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(account.getBalances().get(0).isOpened()).isTrue();  // RUB
        assertThat(account.getBalances().get(1).isOpened()).isTrue();  // USD
        assertThat(account.getBalances().get(2).isOpened()).isFalse(); // EUR
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void updatePassword_ShouldUpdatePassword_WhenValidRequest() {
        AccountPasswordUpdateRequest request = new AccountPasswordUpdateRequest("ivan_ivanov", "new_password");

        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("new_password")).thenReturn("encoded_new_password");
        when(currencyService.getCurrencies()).thenReturn(currencyResponses);
        when(accountMapper.toAccountBalanceResponses(anyList(), anyList()))
                .thenReturn(accountBalanceResponse);
        when(accountMapper.toFullDto(any(Account.class), anyList()))
                .thenReturn(accountFullResponse);

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.updatePassword(request);

        assertThat(account.getPassword()).isEqualTo("encoded_new_password");
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void getCurrentAccount_ShouldReturnFullResponse_WhenAccountExists() {
        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));
        when(currencyService.getCurrencies()).thenReturn(currencyResponses);

        when(accountMapper.toAccountBalanceResponses(anyList(), anyList()))
                .thenReturn(accountBalanceResponse);

        when(accountMapper.toFullDto(any(Account.class), anyList()))
                .thenReturn(accountFullResponse);

        AccountFullResponse response = accountService.getAccountByLogin("ivan_ivanov");

        assertThat(response).isNotNull();
    }

    @Test
    void getCurrentAccount_ShouldThrow_WhenAccountNotFound() {
        when(accountRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountByLogin("unknown"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void getAllAccounts_ShouldReturnListOfResponses() {
        List<Account> accounts = Arrays.asList(account);
        List<AccountResponse> dtos = Arrays.asList(new AccountResponse("ivan_ivanov", "Иван Иванов"));

        when(accountRepository.findAll()).thenReturn(accounts);
        when(accountMapper.toDto(account)).thenReturn(dtos.get(0));

        List<AccountResponse> result = accountService.getAllAccounts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLogin()).isEqualTo("ivan_ivanov");
    }

    @Test
    void findAccountByLogin_ShouldReturnAccount_WhenExists() {
        when(accountRepository.findByLogin("ivan_ivanov")).thenReturn(Optional.of(account));

        Account found = accountService.findAccountByLogin("ivan_ivanov");

        assertThat(found).isEqualTo(account);
    }

    @Test
    void findAccountByLogin_ShouldThrow_WhenNotFound() {
        when(accountRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findAccountByLogin("unknown"))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
