package com.teste.cqrs_bank;

import com.teste.cqrs_bank.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class CqrsBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(CqrsBankApplication.class, args);
    }

}
