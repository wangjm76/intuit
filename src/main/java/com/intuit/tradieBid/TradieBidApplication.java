package com.intuit.tradieBid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class TradieBidApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradieBidApplication.class, args);
    }

}
