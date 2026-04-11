package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Сервер конфигурации (Spring Cloud Config Server).
 * Раздаёт конфигурационные файлы всем микросервисам
 * из локальных ресурсов (classpath:config/).
 * Поддерживает профили: stats, infra, core.
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigServer {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }

}