package com.alextim.bank.cache.service;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashMetricsService {

    private final MeterRegistry meterRegistry;

    private static final String CASH_OPERATIONS_COUNTER_NAME = "custom_cash_operations_total";

    public void incrementCashOperation(String operation, String result) {
        Counter.builder(CASH_OPERATIONS_COUNTER_NAME)
                .description("Total number of cash operations")
                .tag("operation", operation)
                .tag("result", result)
                .register(meterRegistry)
                .increment(); // Увеличиваем счетчик на 1
        log.debug("Incremented cash operation counter for operation: {}, result: {}", operation, result);
    }
}
