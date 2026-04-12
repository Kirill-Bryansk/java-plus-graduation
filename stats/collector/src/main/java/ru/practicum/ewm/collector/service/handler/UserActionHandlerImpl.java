package ru.practicum.ewm.collector.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.collector.service.UserActionProducer;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.event.UserActionProto;

import java.time.Instant;

/**
 * Реализация обработчика: маппит Proto → Avro и отправляет в Kafka.
 *
 * Ключевые моменты:
 * - Enum маппинг через valueOf(name()) — имена enum совпадают (VIEW, REGISTER, LIKE)
 * - Timestamp конвертируется через ofEpochSecond(seconds, nanos) → Instant
 * - Avro создаётся через newBuilder() — корректный способ для генерированных классов
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class UserActionHandlerImpl implements UserActionHandler {

    private final UserActionProducer producer;

    @Override
    public void handle(UserActionProto userActionProto) {
        log.debug("Обработка действия: userId={}, eventId={}, type={}",
                userActionProto.getUserId(),
                userActionProto.getEventId(),
                userActionProto.getActionType());

        producer.sendUserAction(mapToAvro(userActionProto));
    }

    /**
     * Маппинг Proto → Avro.
     * Enum имена совпадают (VIEW/REGISTER/LIKE), поэтому valueOf работает напрямую.
     */
    private UserActionAvro mapToAvro(UserActionProto userActionProto) {
        Instant timestamp = Instant.ofEpochSecond(
                userActionProto.getTimestamp().getSeconds(),
                userActionProto.getTimestamp().getNanos()
        );

        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setActionType(ActionTypeAvro.valueOf(userActionProto.getActionType().name()))
                .setTimestamp(timestamp)
                .build();
    }
}
