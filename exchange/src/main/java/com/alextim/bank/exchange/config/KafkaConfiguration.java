package com.alextim.bank.exchange.config;


import com.alextim.bank.common.property.ErrorKafkaTopicProperty;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ErrorKafkaTopicProperty topicProperty;

    @Bean
    CommonErrorHandler commonErrorHandler() {
        ConsumerRecordRecoverer recordRecoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, e) -> new TopicPartition(topicProperty.getName(), topicProperty.getPartition())
        );

        BackOff backOff = new FixedBackOff(0, 0);
        return new DefaultErrorHandler(recordRecoverer, backOff);
    }
}
