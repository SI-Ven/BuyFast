package com.example.buyfast;

import org.mybatis.spring.annotation.MapperScan; // <-- IMPORT THIS
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.buyfast.modules") // <-- ADD THIS LINE
public class BuyFastApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuyFastApplication.class, args);
    }

}