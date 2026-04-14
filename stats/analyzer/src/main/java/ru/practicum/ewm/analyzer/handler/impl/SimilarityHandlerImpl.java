package ru.practicum.ewm.analyzer.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.dal.model.EventSimilarity;
import ru.practicum.ewm.analyzer.dal.repository.EventSimilarityRepository;
import ru.practicum.ewm.analyzer.handler.SimilarityHandler;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Optional;

/**
 * Сохраняет сходство мероприятий в БД.
 * Обновляет score, если пара уже существует.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SimilarityHandlerImpl implements SimilarityHandler {

    private final EventSimilarityRepository repository;

    @Override
    public void handle(EventSimilarityAvro similarity) {
        Optional<EventSimilarity> existing = repository.findByEventAAndEventB(
                similarity.getEventA(), similarity.getEventB());

        EventSimilarity entity = existing.orElseGet(() -> EventSimilarity.builder()
                .eventA(similarity.getEventA())
                .eventB(similarity.getEventB())
                .build());

        entity.setScore(similarity.getScore());
        repository.save(entity);
        log.debug("Сохранено сходство: {} <-> {} score={}", similarity.getEventA(), similarity.getEventB(), similarity.getScore());
    }
}
