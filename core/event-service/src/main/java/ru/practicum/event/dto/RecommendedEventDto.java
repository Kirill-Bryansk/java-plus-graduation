package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для рекомендованного мероприятия.
 * Возвращается эндпоинтом GET /events/recommendations.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedEventDto {
    /** Идентификатор рекомендуемого мероприятия */
    private long eventId;
    /** Предсказанная оценка (рейтинг) */
    private double score;
}
