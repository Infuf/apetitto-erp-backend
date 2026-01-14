package com.apetitto.apetittoerpbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ApetittoErpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApetittoErpBackendApplication.class, args);
    }

}
