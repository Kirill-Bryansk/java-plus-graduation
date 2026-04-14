package ru.practicum.ewm.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Точка входа сервиса Analyzer.
 * Читает действия и сходства из Kafka, сохраняет в БД.
 * Предоставляет рекомендации через gRPC.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnalyzerApplication.class, args);
    }
}
