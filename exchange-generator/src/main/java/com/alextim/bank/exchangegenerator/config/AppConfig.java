package com.alextim.bank.exchangegenerator.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(basePackages = "com.alextim.bank.common.client")
public class AppConfig {
}
