package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Сервис обнаружения сервисов (Eureka Server).
 * Реестр, в котором регистрируются все микросервисы.
 * Позволяет сервисам находить друг друга по имени без жёсткой привязки к хосту/порту.
 */
@EnableEurekaServer
@SpringBootApplication
public class DiscoveryServer {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServer.class, args);
    }
}
