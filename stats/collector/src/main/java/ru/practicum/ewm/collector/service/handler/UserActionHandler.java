package ru.practicum.ewm.collector.service.handler;

import ru.practicum.ewm.stats.proto.event.UserActionProto;

/**
 * Обработчик gRPC-сообщения о действии пользователя.
 * Отвечает за маппинг Proto → Avro и отправку в Kafka.
 */
public interface UserActionHandler {

    /**
     * Обработать действие пользователя.
     *
     * @param userActionProto Proto-сообщение с действием
     */
    void handle(UserActionProto userActionProto);
}
