package com.alextim.bank.common.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "kafka.error-topic")
@Getter
@Setter
@Component
public class ErrorKafkaTopicProperty {
    private String name;
    private int partition;
}
