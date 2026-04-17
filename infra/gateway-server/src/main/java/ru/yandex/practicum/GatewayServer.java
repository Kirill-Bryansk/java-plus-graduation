package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway — единая точка входа для всех внешних запросов.
 * Маршрутизирует запросы к соответствующим микросервисам
 * на основе конфигурации routes в Config Server.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServer {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServer.class, args);
    }
}