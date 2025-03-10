package com.zanddemo.creditcard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan("com.zanddemo.creditcard.repository")
public class CreditCardApplication {
    public static void main(String[] args) {
        SpringApplication.run(CreditCardApplication.class, args);
    }
}