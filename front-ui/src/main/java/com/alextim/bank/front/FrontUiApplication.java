package com.alextim.bank.front;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.alextim.bank")
public class FrontUiApplication {
    public static void main(String[] args) {
        SpringApplication.run(FrontUiApplication.class, args);
    }
}
