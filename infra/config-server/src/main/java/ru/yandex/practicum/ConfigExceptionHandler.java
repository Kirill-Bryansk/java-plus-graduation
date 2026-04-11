package ru.yandex.practicum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Глобальный обработчик исключений для Config Server.
 * Обрабатывает HttpClientErrorException.NotFound —
 * когда запрошенная конфигурация не найдена.
 */
@ControllerAdvice
public class ConfigExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ConfigExceptionHandler.class);

    /**
     * Обработка ошибки "конфигурация не найдена" (404).
     *
     * @param ex исключение NotFoundException
     */
    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public void handleNotFound(HttpClientErrorException.NotFound ex) {
        log.debug("❌ Конфигурация не найдена: {}", ex.getMessage());
    }
}