package com.alextim.bank.account.service;

import com.alextim.bank.account.constant.Role;
import com.alextim.bank.account.entity.Account;
import com.alextim.bank.account.entity.Balance;
import com.alextim.bank.account.entity.Contact;
import com.alextim.bank.account.exception.AccountNotFoundException;
import com.alextim.bank.account.exception.ContactAlreadyExistsException;
import com.alextim.bank.account.mapper.AccountMapper;
import com.alextim.bank.account.repository.AccountRepository;
import com.alextim.bank.account.repository.ContactRepository;
import com.alextim.bank.common.client.NotificationServiceClient;
import com.alextim.bank.common.dto.account.*;
import com.alextim.bank.common.dto.exchange.CurrencyResponse;
import com.alextim.bank.common.dto.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.alextim.bank.account.constant.ContactType.EMAIL;
import static com.alextim.bank.account.constant.ContactType.TELEGRAM;
import static com.alextim.bank.common.client.util.NotificationClientUtils.sendNotification;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final ContactRepository contactRepository;

    private final AccountMapper accountMapper;

    private final PasswordEncoder passwordEncoder;

    private final CurrencyService currencyService;

    private final NotificationServiceClient notificationServiceClient;

    @Override
    public AccountFullResponse createAccount(AccountRequest request) {
        log.info("Creating new account with login: {}", request.getLogin());

        validateContactUniqueness(request);

        Account account = accountMapper.toEntity(request);
        log.debug("Mapped request to account entity: {}", account);

        log.debug("Fetching currencies for mapping account balances");
        List<CurrencyResponse> currencies = currencyService.getCurrencies();
        log.info("Fetched {} currencies for initializing balances", currencies.size());

        List<Balance> balances = currencies.stream()
                .map(currency -> Balance.builder()
                        .currencyCode(currency.getCode())
                        .account(account)
                        .build())
                .toList();
        account.setBalances(balances);

        List<Contact> contacts = List.of(
                new Contact(EMAIL, request.getEmail(), account),
                new Contact(TELEGRAM, request.getTelegram(), account)
        );
        account.setContacts(contacts);

        account.setRoles(List.of(Role.USER));

        log.info("Saving new account with login: {}", account.getLogin());
        Account savedAccount = accountRepository.save(account);
        log.info("Successfully created account: {}", savedAccount);

        List<AccountBalanceResponse> accountBalanceResponse = accountMapper.toAccountBalanceResponses(
                currencies, savedAccount.getBalances());
        AccountFullResponse response = accountMapper.toFullDto(account, accountBalanceResponse);

        sendNotification(notificationServiceClient,
                new NotificationRequest(savedAccount.getLogin(), "Your account has been successfully created"));

        log.debug("Final response payload for login {}: {}", response.getLogin(), response);
        return response;
    }

    @Transactional
    @Override
    public AccountFullResponse updateAccount(AccountUpdateRequest request) {
        log.info("Updating account with login: {}", request.getLogin());

        Account account = findAccountByLogin(request.getLogin());
        log.debug("Found account for update: {}", account);

        if (request.getName() != null) {
            String[] nameParts = request.getName().trim().split("\\s+");
            if (nameParts.length < 2) {
                log.warn("Invalid name format for login {}: '{}'", request.getLogin(), request.getName());
                throw new IllegalArgumentException("Name must contain at least first and last name");
            }
            account.setFirstName(nameParts[0]);
            account.setLastName(nameParts[1]);
        }

        if (request.getBirthDate() != null) {
            account.setBirthDate(request.getBirthDate());
        }

        if (request.getCurrencyCodes() != null) {
            account.getBalances().forEach(balance -> balance.setOpened(false));

            Set<String> requestedCurrencies = new HashSet<>(request.getCurrencyCodes());

            account.getBalances().forEach(balance -> {
                        if (requestedCurrencies.contains(balance.getCurrencyCode())) {
                            balance.setOpened(true);
                        }
                    }
            );
        }

        log.info("Updated balances: {} opened for account: {}", account.getBalances().stream()
                .filter(Balance::isOpened)
                .map(Balance::getCurrencyCode)
                .toList(), account.getLogin());

        Account updatedAccount = accountRepository.save(account);
        log.info("Successfully updated account: {}", updatedAccount.getLogin());

        List<AccountBalanceResponse> accountBalanceResponse = accountMapper.toAccountBalanceResponses(
                currencyService.getCurrencies(), account.getBalances());
        AccountFullResponse response = accountMapper.toFullDto(updatedAccount, accountBalanceResponse);

        sendNotification(notificationServiceClient,
                new NotificationRequest(updatedAccount.getLogin(), "Your account has been successfully updated"));

        log.debug("Final response payload for login {}: {}", response.getLogin(), response);
        return response;
    }

    private void validateContactUniqueness(AccountRequest request) {
        log.debug("Validating contact uniqueness for email: {}, telegram: {}", request.getEmail(), request.getTelegram());

        if (contactRepository.existsByTypeAndValue(EMAIL, request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new ContactAlreadyExistsException(EMAIL, request.getEmail());
        }

        if (contactRepository.existsByTypeAndValue(TELEGRAM, request.getTelegram())) {
            log.warn("Telegram contact already exists: {}", request.getTelegram());
            throw new ContactAlreadyExistsException(TELEGRAM, request.getTelegram());
        }
    }

    @Override
    public AccountFullResponse updatePassword(AccountPasswordUpdateRequest request) {
        log.info("Updating password for login: {}", request.getLogin());

        Account account = findAccountByLogin(request.getLogin());
        log.debug("Found account for password update: {}", account);

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        log.debug("Password updated for login: {}", request.getLogin());

        Account updatedAccount = accountRepository.save(account);
        log.info("Password successfully updated for login: {}", updatedAccount.getLogin());

        List<AccountBalanceResponse> accountBalanceResponse = accountMapper.toAccountBalanceResponses(
                currencyService.getCurrencies(), account.getBalances());
        AccountFullResponse response = accountMapper.toFullDto(updatedAccount, accountBalanceResponse);

        sendNotification(notificationServiceClient, new NotificationRequest(updatedAccount.getLogin(), "Your account has been successfully updated"));

        log.debug("Final response payload for login {}: {}", response.getLogin(), response);
        return response;
    }

    @Override
    public AccountFullResponse getAccountByLogin(String login) {
        log.info("Fetching full account info for login: {}", login);

        Account account = findAccountByLogin(login);
        log.debug("Found account for get: {}", account);

        account.setBalances(getSortedBalances(account.getBalances()));
        log.debug("Sorted balances by amount (desc) for login: {}", login);

        List<AccountBalanceResponse> accountBalanceResponse = accountMapper.toAccountBalanceResponses(
                currencyService.getCurrencies(), account.getBalances());
        AccountFullResponse response = accountMapper.toFullDto(account, accountBalanceResponse);

        log.debug("Final response payload for login {}: {}", response.getLogin(), response);
        return response;
    }

    Account findAccountByLogin(String login) {
        return accountRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException(login));
    }

    List<Balance> getSortedBalances(List<Balance> balances) {
        return balances.stream()
                .sorted(Comparator.comparing(Balance::getAmount, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        log.info("Fetching all accounts");

        List<Account> accounts = accountRepository.findAll();
        log.info("Found {} accounts", accounts.size());

        List<AccountResponse> responses = accounts.stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());

        log.debug("Final response payload: {}", responses);
        return responses;
    }

    @Override
    public AccountStatusResponse getAccountStatus(String login) {
        log.info("Fetching account status for login: {}", login);

        boolean isBlocked = findAccountByLogin(login).isBlocked();
        log.info("Account with login '{}' is {}", login, isBlocked ? "blocked" : "not blocked");

        AccountStatusResponse response = new AccountStatusResponse(login, isBlocked);

        log.debug("Final response payload for login {}: {}", response.getLogin(), response);
        return response;
    }

    @Override
    public AccountContactsResponse getAccountContacts(String login) {
        log.info("Fetching contacts for login: {}", login);

        List<Contact> contacts = contactRepository.findByAccount_Login(login);
        log.debug("Found {} contacts for login: {}", contacts.size(), login);

        Map<String, String> contactMap = contacts.stream().
                collect(Collectors.toMap(contact -> contact.getType().name(), Contact::getValue));
        log.info("Contacts retrieved for login {}: {}", login, contactMap.keySet());

        AccountContactsResponse response = new AccountContactsResponse(login, contactMap);

        log.debug("Final response payload for login {}: {}", response.getLogin(), response);
        return response;
    }
}
