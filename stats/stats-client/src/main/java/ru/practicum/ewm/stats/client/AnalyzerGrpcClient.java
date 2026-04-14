package ru.practicum.ewm.stats.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.dashboard.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.dashboard.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.dashboard.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.dashboard.UserPredictionsRequestProto;
import ru.practicum.ewm.stats.proto.dashboard.RecommendationsControllerGrpc;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * gRPC-клиент для получения рекомендаций от Analyzer.
 * <p>
 * Предоставляет три метода:
 * тп
 *   <li>{@link #getRecommendationsForUser} — персональные рекомендации (на основе предсказания оценки)</li>
 *   <li>{@link #getSimilarEvents} — мероприятия, похожие на указанное</li>
 *   <li>{@link #getInteractionsCount} — сумма весов взаимодействий по списку мероприятий</li>
 * </ul>
 * <p>
 * Конфигурация адреса (в application.yaml сервиса-потребителя):
 * <pre>
 * grpc:
 *   client:
 *     analyzer:
 *       address: 'discovery:///analyzer'
 *       enableKeepAlive: true
 *       keepAliveWithoutCalls: true
 *       negotiationType: plaintext
 * </pre>
 */
@Service
@Slf4j
public class AnalyzerGrpcClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    /**
     * Получить персональные рекомендации для пользователя.
     * Алгоритм: на основе истории взаимодействий и косинусного сходства
     * предсказывает оценку для мероприятий, с которыми пользователь ещё не взаимодействовал.
     *
     * @param userId     идентификатор пользователя
     * @param maxResults максимальное количество рекомендаций
     * @return поток RecommendedEventProto (eventId + предсказанная оценка)
     */
    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        try {
            Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(request);
            return asStream(iterator);
        } catch (Exception e) {
            log.error("Ошибка получения рекомендаций для userId={}: {}", userId, e.getMessage());
            return Stream.empty();
        }
    }

    /**
     * Получить мероприятия, похожие на указанное.
     * Исключает мероприятия, с которыми пользователь уже взаимодействовал.
     *
     * @param eventId    идентификатор мероприятия-образца
     * @param userId     идентификатор пользователя (для фильтрации просмотренных)
     * @param maxResults максимальное количество результатов
     * @return поток RecommendedEventProto (eventId + коэффициент сходства)
     */
    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        try {
            Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(request);
            return asStream(iterator);
        } catch (Exception e) {
            log.error("Ошибка получения похожих событий для eventId={}, userId={}: {}",
                    eventId, userId, e.getMessage());
            return Stream.empty();
        }
    }

    /**
     * Получить сумму максимальных весов действий для списка мероприятий.
     * Используется для расчёта рейтинга (популярности) мероприятий.
     *
     * @param eventIds список идентификаторов мероприятий
     * @return поток RecommendedEventProto (eventId + сумма весов)
     */
    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        try {
            Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);
            return asStream(iterator);
        } catch (Exception e) {
            log.error("Ошибка получения счётчика взаимодействий: {}", e.getMessage());
            return Stream.empty();
        }
    }

    /**
     * Преобразовать Iterator в Stream для удобной обработки в Java.
     * gRPC-методы возвращают Iterator, потому что в схеме указан stream.
     */
    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
