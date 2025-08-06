package com.alextim.bank.exchangegenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.alextim.bank")
public class ExchangeGeneratorApp {
    public static void main(String[] args) {
        SpringApplication.run(ExchangeGeneratorApp.class, args);
    }
}
