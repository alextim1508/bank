package com.alextim.bank.blocker.property;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "blocker")
@Getter
@Setter
@ToString
public class BlockerProperties {

    private BigDecimal maxAmount;

    private int maxOperations;

    private int timeWindowMinutes;

    private final Night night = new Night();

    public Duration getTimeWindow() {
        return Duration.ofMinutes(timeWindowMinutes);
    }

    @Setter
    @Getter
    @ToString
    public static class Night {
        private int startHour;
        private int endHour;
    }
}
