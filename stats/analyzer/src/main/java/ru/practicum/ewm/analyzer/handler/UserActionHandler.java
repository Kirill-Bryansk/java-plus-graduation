package ru.practicum.ewm.analyzer.handler;

import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * Обработчик действий пользователей из Kafka.
 */
public interface UserActionHandler {
    void handle(UserActionAvro action);
}
