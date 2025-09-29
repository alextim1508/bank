package com.alextim.bank.exchange.service;

import com.alextim.bank.common.dto.exchange.UpdateRatesRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaRatesConsumer {

    private final ConvertService service;

    @KafkaListener( topics = "#{@ratesKafkaTopicProperty.name}",
                    groupId = "${spring.kafka.consumer.group-id}")
    public void consumeRatesUpdate(UpdateRatesRequest message) {
        log.info("Received rates update: {}", message);

        Map<String, Double> rates = message.getRates();

        service.updateRates(rates);
        log.info("Successfully updated {} rate(s)", rates.size());
    }
}