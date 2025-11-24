package com.example.buyfast;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.buyfast.**.repository")
public class BuyFastApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuyFastApplication.class, args);
    }

}
