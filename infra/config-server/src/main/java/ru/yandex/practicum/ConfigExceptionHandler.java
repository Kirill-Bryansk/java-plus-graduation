package ru.yandex.practicum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class ConfigExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ConfigExceptionHandler.class);

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    public void handleNotFound(HttpClientErrorException.NotFound ex) {
        log.debug("❌ Конфигурация не найдена: {}", ex.getMessage());
    }
}