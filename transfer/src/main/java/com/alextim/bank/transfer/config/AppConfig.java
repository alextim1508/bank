package com.alextim.bank.transfer.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.alextim.bank.common.client")
public class AppConfig {
}
