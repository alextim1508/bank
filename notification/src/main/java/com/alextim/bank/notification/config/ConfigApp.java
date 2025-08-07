package com.alextim.bank.notification.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableDiscoveryClient
@EnableCaching
@EnableScheduling
@EnableFeignClients(basePackages = "com.alextim.bank.common.client")
public class ConfigApp {
}
