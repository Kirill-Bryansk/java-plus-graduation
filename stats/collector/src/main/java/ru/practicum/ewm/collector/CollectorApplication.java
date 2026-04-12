package ru.practicum.ewm.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Точка входа сервиса Collector.
 * Принимает действия пользователей по gRPC и отправляет их в Kafka.
 */
@SpringBootApplication
@EnableDiscoveryClient  // регистрация в Eureka
@EnableRetry            // включение retry при подключении к config-server
public class CollectorApplication {
    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }
}
