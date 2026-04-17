package ru.practicum.grpc.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    /**
     * Retryer для Feign-клиентов.
     * Помогает в CI, когда сервисы не сразу регистрируются в Eureka.
     * 5 попыток с начальным интервалом 100мс и максимальным 1сек.
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 1000, 5);
    }
}


