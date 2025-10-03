package com.alextim.bank.account.service;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthMetricsService {

    private final MeterRegistry meterRegistry;

    private static final String LOGIN_FAILURE_COUNTER_NAME = "custom_login_failure_total";

    private static final String LOGIN_SUCCESS_COUNTER_NAME = "custom_login_success_total";

    public void incrementLoginFailure(String login) {
        Counter.builder(LOGIN_FAILURE_COUNTER_NAME)
                .description("Total number of failed login attempts per user")
                .tag("login", login)
                .register(meterRegistry)
                .increment();
        log.debug("Incremented login failure counter for login: {}", login);
    }

    public void incrementLoginSuccess(String login) {
        Counter.builder(LOGIN_SUCCESS_COUNTER_NAME)
                .description("Total number of successful login attempts per user")
                .tag("login", login)
                .register(meterRegistry)
                .increment();
        log.debug("Incremented login success counter for login: {}", login);
    }
}
