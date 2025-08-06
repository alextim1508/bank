package com.alextim.bank.account.service;

public interface BlacklistService {
    void addToBlacklist(String token, long ttlMillis);

    boolean isBlacklisted(String token);
}
