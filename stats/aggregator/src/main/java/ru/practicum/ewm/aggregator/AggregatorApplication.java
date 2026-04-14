package ru.practicum.ewm.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Точка входа сервиса Aggregator.
 * Читает действия пользователей из Kafka, вычисляет сходство мероприятий
 * и отправляет результаты в Kafka-топик stats.events-similarity.v1.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AggregatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AggregatorApplication.class, args);
    }
}
