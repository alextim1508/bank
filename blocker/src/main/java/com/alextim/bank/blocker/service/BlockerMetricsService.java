package com.alextim.bank.blocker.service;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockerMetricsService {

    private final MeterRegistry meterRegistry;

    private static final String TRANSFER_BLOCKED_COUNTER_NAME = "custom_transfer_blocked_total";

    public void incrementTransferBlocked(String login, String reason) {
        Counter.builder(TRANSFER_BLOCKED_COUNTER_NAME)
                .description("Total number of blocked transfers per user and reason")
                .tag("login", login)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
        log.debug("Incremented transfer blocked counter for login: {}, reason: {}", login, reason);
    }
}