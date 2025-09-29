package com.alextim.bank.exchangegenerator.service;

import com.alextim.bank.common.dto.exchange.UpdateRatesRequest;
import com.alextim.bank.common.property.RatesKafkaTopicProperty;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Setter
@RequiredArgsConstructor
@Slf4j
public class ExchangeGenerator {

    private final Random random = new Random();

    private final KafkaTemplate<String, UpdateRatesRequest> kafkaTemplate;
    private final RatesKafkaTopicProperty topicProperty;

    public Map<String, Double> generateRates() {
        Map<String, Double> rates = new HashMap<>();

        rates.put("RUB", 1.0);

        double usdRate = 60 + (100 - 60) * random.nextDouble();
        rates.put("USD", usdRate);

        double eurRate = 8 + (12 - 8) * random.nextDouble();
        rates.put("EUR", eurRate);

        double cnyRate = 8 + (12 - 8) * random.nextDouble();
        rates.put("CNY", cnyRate);

        double gbpRate = 8 + (12 - 8) * random.nextDouble();
        rates.put("GBP", gbpRate);

        return rates;
    }

    @Scheduled(fixedRate = 2_000)
    public void updateRates() {
        try {
            Map<String, Double> rates = generateRates();
            log.info("Generated rates: {}", rates);

            kafkaTemplate.send(
                    topicProperty.getName(),
                    topicProperty.getKey(),
                    new UpdateRatesRequest(rates))
                        .whenComplete((result, e) -> {
                            if (e != null) {
                                log.error("Error sending message: {}", e.getMessage(), e);
                                return;
                            }

                            RecordMetadata metadata = result.getRecordMetadata();
                            log.info("Message sent. Topic = {}, partition = {}, offset = {}",
                                    metadata.topic(), metadata.partition(), metadata.offset());
                        });
        } catch (Exception e) {
            log.error("Error updating exchange rates", e);
        }
    }
}
