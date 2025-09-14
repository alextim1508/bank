package com.alextim.bank.account.service;

import com.alextim.bank.common.dto.account.*;

import java.util.List;

public interface AccountService {

    AccountFullResponse createAccount(AccountRequest request);

    AccountFullResponse updateAccount(AccountUpdateRequest request);

    AccountFullResponse updatePassword(AccountPasswordUpdateRequest accountDTO);

    AccountFullResponse getAccountByLogin(String login);

    List<AccountResponse> getAllAccounts();

    AccountStatusResponse getAccountStatus(String login);

    AccountContactsResponse getAccountContacts(String login);
}
