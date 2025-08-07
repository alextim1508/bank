package com.alextim.bank.notification.service;

import com.alextim.bank.common.client.AccountServiceClient;
import com.alextim.bank.common.dto.account.AccountContactsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

import static com.alextim.bank.common.client.util.AccountClientUtils.getAccountContacts;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachingAccountContactServiceImpl implements AccountContactService {

    private final AccountServiceClient client;

    @Override
    @Cacheable(
            value = "accountContacts",
            key = "#login",
            sync = true,
            unless = "#result == null"
    )
    public Map<String, String> getContacts(String login) {
        log.info("Cache miss for contacts of login: {}", login);

        AccountContactsResponse response = getAccountContacts(client, login);
        if (response == null || response.getContacts() == null) {
            log.warn("No contacts found for login: {}", login);
            return Collections.emptyMap();
        }

        log.info("Fetched {} contacts for login: {} from Account service", response.getContacts().size(), login);
        return response.getContacts();
    }
}
