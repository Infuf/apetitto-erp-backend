package com.apetitto.apetittoerpbackend.erp.common.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

@SpringBootTest
@ContextConfiguration(initializers = ContainerConfig.Initializer.class)
public class ContainerConfig {
    public static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        Startables.deepStart(postgresContainer).join();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
            System.setProperty("spring.datasource.username", postgresContainer.getUsername());
            System.setProperty("spring.datasource.password", postgresContainer.getPassword());
            System.setProperty("spring.liquibase.enabled", "true");
        }
    }
}
