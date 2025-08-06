package com.alextim.bank.account.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDiscoveryClient
@EnableCaching
@EnableFeignClients(basePackages = "com.alextim.bank.common.client")
public class AppConfig {
}
