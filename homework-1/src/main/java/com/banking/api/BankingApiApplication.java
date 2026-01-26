package com.banking.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Spring Boot application class for Banking Transactions API.
 * 
 * @author AI Coding Partner
 * @version 1.0.0
 */
@SpringBootApplication
public class BankingApiApplication {

    private static final Logger log = LoggerFactory.getLogger(BankingApiApplication.class);

    public static void main(String[] args) {
        log.info("Starting Banking Transactions API...");
        SpringApplication.run(BankingApiApplication.class, args);
        log.info("Banking Transactions API started successfully!");
    }
}
