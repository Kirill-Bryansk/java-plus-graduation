package ru.practicum.ewm.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.collector.service.handler.UserActionHandler;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.event.UserActionProto;

/**
 * gRPC-контроллер Collector.
 * Принимает UserActionProto от core-сервисов и делегирует обработку в Handler.
 *
 * Отличия от рыбы:
 * - Пакет ru.practicum.ewm.collector (наша уникальность)
 * - Подробное логирование входящих запросов
 * - Обработка ошибок через responseObserver.onError()
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcUserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final UserActionHandler userActionHandler;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        log.debug("gRPC-запрос: userId={}, eventId={}, actionType={}",
                request.getUserId(), request.getEventId(), request.getActionType());

        try {
            userActionHandler.handle(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке действия: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(e.getMessage())));
        }
    }
}
