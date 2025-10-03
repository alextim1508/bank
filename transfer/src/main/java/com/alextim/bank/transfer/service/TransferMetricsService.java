package com.alextim.bank.transfer.service;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferMetricsService {

    private final MeterRegistry meterRegistry;

    private static final String TRANSFER_OPERATIONS_COUNTER_NAME = "custom_transfer_operations_total";

    public void incrementTransferOperation(String operation, String result) {
        incrementTransferOperation(operation, result, null);
    }

    public void incrementTransferOperation(String operation, String result, String side) {
        var tagsBuilder = Tags.of("operation", operation, "result", result);
        if (side != null) {
            tagsBuilder = tagsBuilder.and("side", side);
        }

        Counter.builder(TRANSFER_OPERATIONS_COUNTER_NAME)
                .description("Total number of transfer operations")
                .tags(tagsBuilder)
                .register(meterRegistry)
                .increment();
        log.debug("Incremented transfer operation counter for operation: {}, result: {}, side: {}", operation, result, side);
    }
}