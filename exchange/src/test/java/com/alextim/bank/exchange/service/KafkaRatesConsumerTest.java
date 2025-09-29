package com.alextim.bank.exchange.service;

import com.alextim.bank.common.dto.exchange.UpdateRatesRequest;
import com.alextim.bank.common.property.RatesKafkaTopicProperty;
import com.alextim.bank.exchange.config.KafkaConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.event.ListenerContainerIdleEvent;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

@ActiveProfiles("test")
@SpringBootTest(
        classes = {KafkaRatesConsumer.class, RatesKafkaTopicProperty.class}
)
@EmbeddedKafka(
        partitions = 1,
        topics = {"rates-topic", "rates-topic.errors"},
        brokerProperties = {"auto.create.topics.enable=true"}
)
@Import({KafkaConfiguration.class, KafkaAutoConfiguration.class, KafkaRatesConsumerTest.TestKafkaConfig.class})
@DirtiesContext
class KafkaRatesConsumerTest {

    @Autowired
    private KafkaTemplate<String, UpdateRatesRequest> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @MockitoBean
    private ConvertService convertService;

    @TestConfiguration
    @EnableKafka
    static class TestKafkaConfig {
        @Component
        public class KafkaConsumerReadyLatch {

            private final CountDownLatch latch = new CountDownLatch(1);

            @Value("${spring.kafka.consumer.group-id}")
            private String groupId;

            @EventListener
            public void handleIdle(ListenerContainerIdleEvent event) {
                if (event.getListenerId().contains(groupId)) {
                    latch.countDown();
                }
            }

            public void await() throws InterruptedException {
                latch.await(60, TimeUnit.SECONDS);
            }
        }
    }

    @Autowired
    private TestKafkaConfig.KafkaConsumerReadyLatch consumerReadyLatch;

    @Test
    void shouldConsumeValidMessage() throws InterruptedException {
        consumerReadyLatch.await();
        kafkaTemplate.send("rates-topic", new UpdateRatesRequest(Map.of("USD", 1.2)));

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> Mockito.verify(convertService).updateRates(anyMap()));
    }

    @Test
    void shouldSendToDLQOnException() throws InterruptedException {
        doThrow(new RuntimeException("Test exception"))
                .when(convertService).updateRates(anyMap());

        consumerReadyLatch.await();

        UpdateRatesRequest request = new UpdateRatesRequest(Map.of("EUR", 1.5));

        kafkaTemplate.send("rates-topic", request);

        Consumer<String, Object> dlqConsumer = createDlqConsumer();
        dlqConsumer.subscribe(Collections.singletonList("rates-topic.errors"));

        await().atMost(Duration.ofSeconds(10))
                .until(() -> {
                    ConsumerRecords<String, Object> records = dlqConsumer.poll(Duration.ofMillis(100));
                    if(records.count() > 0) {
                        ConsumerRecord<String, Object> record = records.iterator().next();
                        if(record.value() instanceof UpdateRatesRequest request1) {
                            return request1.getRates().get("EUR") == 1.5;
                        }
                    }
                    return false;
                });

        Mockito.verify(convertService, times(2)).updateRates(anyMap());
    }

    private Consumer<String, Object> createDlqConsumer() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "dlq-test-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.alextim.bank.common.dto.exchange");
        return new DefaultKafkaConsumerFactory<String, Object>(consumerProps).createConsumer();
    }
}
