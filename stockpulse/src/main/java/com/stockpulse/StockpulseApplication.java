package com.stockpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Background timer jobs ko activate karne ke liye
public class StockpulseApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockpulseApplication.class, args);
    }
}