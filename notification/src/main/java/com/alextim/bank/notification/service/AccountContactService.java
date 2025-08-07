package com.alextim.bank.notification.service;

import org.springframework.cache.annotation.Cacheable;

import java.util.Map;

public interface AccountContactService {
    @Cacheable("currencies")
    Map<String, String> getContacts(String login);
}
