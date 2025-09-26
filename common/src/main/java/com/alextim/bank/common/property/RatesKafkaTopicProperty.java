package com.alextim.bank.common.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "kafka.rate-topic")
@Getter
@Setter
@Component
public class RatesKafkaTopicProperty {
    private String name;
    private String key;
    private int partition;
}
