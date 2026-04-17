package ru.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Конфигурация логирования входящих HTTP-запросов для EWM API.
 * <p>
 * Логирует URL, query-параметры и тело запроса с уникальным префиксом {@code [EWM-REQUEST]}.
 * Позволяет включать/выключать логирование заголовков через свойство {@code spring.http.logging.include-headers}.
 * Исключает шумные системные эндпоинты (actuator, health).
 */
@Configuration
public class EwmRequestLoggingConfig {

    private static final List<String> EXCLUDED_PATHS = List.of("/actuator", "/health");

    @Value("${spring.http.logging.include-headers:false}")
    private boolean includeHeaders;

    @Bean
    public CommonsRequestLoggingFilter ewmRequestLoggingFilter() {
        return new CommonsRequestLoggingFilter() {
            @Override
            protected boolean shouldLog(jakarta.servlet.http.HttpServletRequest request) {
                String path = request.getRequestURI();
                // Не логируем системные проверки
                return EXCLUDED_PATHS.stream().noneMatch(path::startsWith);
            }
        };
    }

    /**
     * Дополнительная настройка фильтра.
     */
    @Bean
    public CommonsRequestLoggingFilter logFilterConfig(CommonsRequestLoggingFilter filter) {
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(includeHeaders);
        filter.setAfterMessagePrefix("[EWM-REQUEST] ");
        return filter;
    }
}
