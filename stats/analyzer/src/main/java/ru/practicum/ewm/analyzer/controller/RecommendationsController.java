package ru.practicum.ewm.analyzer.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.analyzer.dal.repository.EventSimilarityRepository;
import ru.practicum.ewm.analyzer.dal.repository.UserActionRepository;
import ru.practicum.ewm.stats.proto.dashboard.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.dashboard.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.dashboard.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.dashboard.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.dashboard.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * gRPC-сервис рекомендаций.
 * Три метода:
 * 1. GetRecommendationsForUser — персональные рекомендации
 * 2. GetSimilarEvents — похожие мероприятия
 * 3. GetInteractionsCount — сумма взаимодействий
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final UserActionRepository userActionRepo;
    private final EventSimilarityRepository similarityRepo;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        // 1. Получаем последние N событий пользователя
        List<Long> userEventIds = userActionRepo.findEventIdsByUserId(userId,
                org.springframework.data.domain.PageRequest.of(0, 50));

        if (userEventIds.isEmpty()) {
            responseObserver.onCompleted();
            return;
        }

        Set<Long> userEventIdSet = new HashSet<>(userEventIds);

        // 2. Находим похожие мероприятия, которых пользователь ещё не видел
        //    (берём все similarity где хотя бы одно из событий — из userEventIds)
        List<ru.practicum.ewm.analyzer.dal.model.EventSimilarity> neighbours =
                similarityRepo.findByEventIdIn(userEventIdSet,
                        org.springframework.data.domain.PageRequest.of(0, maxResults * 5));

        // 3. Фильтруем уже виденные
        Set<Long> candidates = new HashSet<>();
        for (var s : neighbours) {
            if (!userEventIdSet.contains(s.getEventA()) && !userEventIdSet.contains(s.getEventB())) {
                candidates.add(s.getEventA());
                candidates.add(s.getEventB());
            }
        }
        candidates.removeAll(userEventIdSet);

        // 4. Для каждого кандидата считаем предсказанный score
        List<RecommendedEventProto> results = predictScores(userId, new ArrayList<>(candidates), userEventIds)
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(e -> RecommendedEventProto.newBuilder().setEventId(e.getKey()).setScore(e.getValue()).build())
                .collect(Collectors.toList());

        results.forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();

        // Находим все сходства для этого мероприятия
        List<ru.practicum.ewm.analyzer.dal.model.EventSimilarity> allSimilar =
                similarityRepo.findAllByEventId(eventId);

        // Исключаем мероприятия, с которыми пользователь уже взаимодействовал
        Set<Long> userEvents = userActionRepo.findEventIdsByUserIdExcludeEventId(userId, eventId);

        List<RecommendedEventProto> results = allSimilar.stream()
                .filter(s -> {
                    long other = (s.getEventA() == eventId) ? s.getEventB() : s.getEventA();
                    return !userEvents.contains(other);
                })
                .sorted(Comparator.comparingDouble(ru.practicum.ewm.analyzer.dal.model.EventSimilarity::getScore).reversed())
                .limit(maxResults)
                .map(s -> {
                    long other = (s.getEventA() == eventId) ? s.getEventB() : s.getEventA();
                    return RecommendedEventProto.newBuilder().setEventId(other).setScore(s.getScore()).build();
                })
                .collect(Collectors.toList());

        results.forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        Set<Long> eventIds = new HashSet<>(request.getEventIdList());

        List<Object[]> sums = userActionRepo.sumWeightsByEventId(eventIds);
        Map<Long, Double> countByEvent = new HashMap<>();
        for (Object[] row : sums) {
            countByEvent.put((Long) row[0], (Double) row[1]);
        }

        for (Long eventId : eventIds) {
            double sum = countByEvent.getOrDefault(eventId, 0.0);
            responseObserver.onNext(RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore(sum)
                    .build());
        }
        responseObserver.onCompleted();
    }

    /**
     * Предсказать scores для кандидат-мероприятий на основе взвешенного среднего соседей.
     */
    private Map<Long, Double> predictScores(long userId, List<Long> candidates, List<Long> userEventIds) {
        Map<Long, Double> scores = new HashMap<>();

        for (Long candidate : candidates) {
            // Находим соседей кандидата
            List<ru.practicum.ewm.analyzer.dal.model.EventSimilarity> neighbours =
                    similarityRepo.findAllByEventId(candidate);

            double weightedSum = 0.0;
            double similaritySum = 0.0;

            for (var sim : neighbours) {
                long neighbourEvent = (sim.getEventA() == candidate) ? sim.getEventB() : sim.getEventA();

                // Проверяем, взаимодействовал ли пользователь с соседом
                var actionOpt = userActionRepo.findByUserIdAndEventId(userId, neighbourEvent);
                if (actionOpt.isPresent()) {
                    double weight = actionOpt.get().getWeight();
                    double simScore = sim.getScore();
                    weightedSum += weight * simScore;
                    similaritySum += simScore;
                }
            }

            if (similaritySum > 0) {
                scores.put(candidate, weightedSum / similaritySum);
            }
        }

        return scores;
    }
}
