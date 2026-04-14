package ru.practicum.ewm.stats.client;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.event.ActionTypeProto;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.event.UserActionProto;

import java.time.Instant;

/**
 * gRPC-клиент для отправки действий пользователей в Collector.
 * <p>
 * Collector принимает UserActionProto и записывает их в Kafka-топик
 * stats.user-actions.v1 для последующей обработки Aggregator'ом.
 * <p>
 * Конфигурация адреса (в application.yaml сервиса-потребителя):
 * <pre>
 * grpc:
 *   client:
 *     collector:
 *       address: 'discovery:///collector'
 *       negotiationType: plaintext
 * </pre>
 */
@Service
@Slf4j
public class CollectorGrpcClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    /**
     * Отправить действие пользователя в Collector.
     *
     * @param userId    идентификатор пользователя
     * @param eventId   идентификатор мероприятия
     * @param actionType тип действия (VIEW/REGISTER/LIKE)
     */
    public void sendUserAction(long userId, long eventId, ActionType actionType) {
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        UserActionProto action = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(mapToProto(actionType))
                .setTimestamp(timestamp)
                .build();

        try {
            Empty response = client.collectUserAction(action);
            log.debug("Действие отправлено: userId={}, eventId={}, type={}", userId, eventId, actionType);
        } catch (Exception e) {
            log.error("Ошибка при отправке действия в Collector: userId={}, eventId={}, type={}, error={}",
                    userId, eventId, actionType, e.getMessage());
        }
    }

    /**
     * Маппинг enum ActionType → ActionTypeProto.
     */
    private ActionTypeProto mapToProto(ActionType type) {
        return switch (type) {
            case ACTION_VIEW -> ActionTypeProto.ACTION_VIEW;
            case ACTION_REGISTER -> ActionTypeProto.ACTION_REGISTER;
            case ACTION_LIKE -> ActionTypeProto.ACTION_LIKE;
        };
    }
}
