package ru.practicum.ewm.collector;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.practicum.ewm.stats.proto.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.event.ActionTypeProto;
import ru.practicum.ewm.stats.proto.event.UserActionProto;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;

import java.time.Instant;

/**
 * Тестовый клиент для отправки одного действия в Collector.
 * Запусти этот main-метод, чтобы проверить работу всей цепочки.
 */
public class GrpcTestClient {
    public static void main(String[] args) {
        // ВАЖНО: Укажи здесь порт, который написал Collector при запуске!
        // Ищи строку: "gRPC Server started, listening on address: *, port: XXXXX"
        int port = 23186; // ЗАМЕНИ НА СВОЙ ПОРТ

        String target = "localhost:" + port;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        UserActionControllerGrpc.UserActionControllerBlockingStub stub = UserActionControllerGrpc.newBlockingStub(channel);

        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(1L)
                .setEventId(10L)   // Пользователь 1 посмотрел Событие 10
                .setActionType(ActionTypeProto.ACTION_VIEW)
                .setTimestamp(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .build();

        try {
            System.out.println("Отправляем запрос: " + request);
            Empty response = stub.collectUserAction(request);
            System.out.println("Успех! Ответ сервера: " + response);
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        } finally {
            channel.shutdown();
        }
    }
}