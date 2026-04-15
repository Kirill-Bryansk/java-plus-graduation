package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "ru.practicum") // для клиента
@EnableFeignClients(basePackages = "ru.practicum")
public class RequestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceApplication.class, args);
    }
}