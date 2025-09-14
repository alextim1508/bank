package com.alextim.bank.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.alextim.bank.common.service.JwtServiceImpl.maskToken;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistServiceImpl implements BlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addToBlacklist(String token, long ttlMillis) {
        Duration duration = Duration.ofMillis(ttlMillis);
        log.info("Adding token to blacklist: {} (TTL: {} ms)", maskToken(token), ttlMillis);

        try {
            redisTemplate.opsForValue().set(token, "revoked", duration);
            log.info("Token successfully added to blacklist: {}", maskToken(token));
        } catch (Exception error) {
            log.error("Error adding token {} to blacklist", maskToken(token), error);
            throw new RuntimeException("Failed to add token to blacklist", error);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        log.info("Checking if token is blacklisted: {}", maskToken(token));

        try {
            String value = redisTemplate.opsForValue().get(token);
            boolean blacklisted = "revoked".equals(value);
            if (blacklisted) {
                log.info("Token is blacklisted: {}", maskToken(token));
            } else {
                log.debug("Token is not blacklisted: {}", maskToken(token));
            }
            return blacklisted;
        } catch (Exception error) {
            log.error("Error checking if token {} is blacklisted", maskToken(token), error);
            return false;
        }
    }


}