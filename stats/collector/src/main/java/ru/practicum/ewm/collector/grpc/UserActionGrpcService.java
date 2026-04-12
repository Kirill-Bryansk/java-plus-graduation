package ru.practicum.ewm.collector.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.collector.producer.UserActionKafkaProducer;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.event.UserActionProto;

import java.time.Instant;

/**
 * gRPC-сервис Collector.
 * Принимает UserActionProto от core-сервисов, конвертирует в UserActionAvro
 * и отправляет в Kafka-топик stats.user-actions.v1.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserActionGrpcService extends UserActionControllerGrpc.UserActionControllerImplBase {

    /** Имя Kafka-топика для действий пользователей */
    private static final String TOPIC = "stats.user-actions.v1";

    private final UserActionKafkaProducer kafkaProducer;

    /**
     * Обработать одно действие пользователя.
     *
     * @param request          Proto-сообщение с действием (userId, eventId, actionType, timestamp)
     * @param responseObserver поток для ответа клиенту (Empty)
     */
    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.debug("Получено действие: userId={}, eventId={}, actionType={}",
                request.getUserId(), request.getEventId(), request.getActionType());

        // Конвертация Proto-enum → Avro-enum
        ActionTypeAvro actionTypeAvro = mapToAvro(request.getActionType());

        // Конвертация Proto-Timestamp → Avro-timestamp_ms
        long timestampMs = request.getTimestamp().toEpochMilli();

        // Создание Avro-записи
        UserActionAvro avroMessage = new UserActionAvro(
                request.getUserId(),
                request.getEventId(),
                actionTypeAvro,
                Instant.ofEpochMilli(timestampMs)
        );

        // Ключ партиционирования: действия одного пользователя → одна партиция
        String key = request.getUserId() + ":" + request.getEventId();

        kafkaProducer.send(TOPIC, key, avroMessage);

        // Подтверждение клиенту: действие принято
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    /**
     * Маппинг Proto-enum → Avro-enum.
     * ACTION_VIEW → VIEW, ACTION_REGISTER → REGISTER, ACTION_LIKE → LIKE.
     */
    private ActionTypeAvro mapToAvro(UserActionProto.ActionTypeProto protoType) {
        return switch (protoType) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case UNRECOGNIZED -> throw new IllegalArgumentException(
                    "Нераспознанный ActionTypeProto: " + protoType.getNumber());
        };
    }
}
