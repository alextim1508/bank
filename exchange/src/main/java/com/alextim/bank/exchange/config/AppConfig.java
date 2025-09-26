package com.alextim.bank.exchange.config;


import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableCaching
@EnableFeignClients(basePackages = "com.alextim.bank.common.client")
@EnableKafka
public class AppConfig {
}
