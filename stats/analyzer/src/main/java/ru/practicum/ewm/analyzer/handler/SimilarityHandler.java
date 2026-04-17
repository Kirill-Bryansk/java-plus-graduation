package ru.practicum.ewm.analyzer.handler;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

/**
 * Обработчик сходства мероприятий из Kafka.
 */
public interface SimilarityHandler {
    void handle(EventSimilarityAvro similarity);
}
