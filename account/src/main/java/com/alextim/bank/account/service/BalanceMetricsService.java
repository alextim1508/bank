package com.alextim.bank.account.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceMetricsService {

    private final MeterRegistry meterRegistry;

    private static final String BALANCE_OPERATIONS_COUNTER_NAME = "custom_balance_operations_total";

    public void incrementBalanceOperation(String operation, String result) {
        incrementBalanceOperation(operation, result, null);
    }

    public void incrementBalanceOperation(String operation, String result, String reason) {
        var tagsBuilder = Tags.of("operation", operation, "result", result);
        if (reason != null) {
            tagsBuilder = tagsBuilder.and("reason", reason);
        }

        Counter.builder(BALANCE_OPERATIONS_COUNTER_NAME)
                .description("Total number of balance operations")
                .tags(tagsBuilder)
                .register(meterRegistry)
                .increment();
        log.debug("Incremented balance operation counter for operation: {}, result: {}, reason: {}", operation, result, reason);
    }
}