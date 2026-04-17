package ru.practicum.rating.service;

import ru.practicum.dto.rating.EventSearchByRatingParam;

import java.util.List;

/**
 * Внутренний сервис оценок для межсервисного взаимодействия.
 * Используется event-service для получения самых популярных событий.
 */
public interface RatingInternalService {

    /**
     * Получить ID самых лайкнутых событий.
     *
     * @param param параметры запроса (limit)
     * @return список ID событий, отсортированный по количеству лайков
     */
    List<Long> getMostLikedEventIds(EventSearchByRatingParam param);
}
