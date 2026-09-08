package com.bank.td;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TermDepositApplication {

    public static void main(String[] args) {
        SpringApplication.run(TermDepositApplication.class, args);
    }
}
