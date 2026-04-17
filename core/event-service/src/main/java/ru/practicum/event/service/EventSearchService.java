package ru.practicum.event.service;

import ru.practicum.dto.rating.EventSearchByRatingParam;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

/**
 * Сервис поиска событий по рейтингу.
 * Предоставляет методы для поиска самых популярных событий
 * на основе лайков из rating-service.
 */
public interface EventSearchService {

    /**
     * Найти самые популярные события по количеству лайков.
     * Вызывает rating-service через Feign для получения рейтингов.
     *
     * @param eventSearchByRatingParam параметры поиска (limit)
     * @return список EventShortDto, отсортированный по рейтингу
     */
    List<EventShortDto> searchMostLikedEvents(EventSearchByRatingParam eventSearchByRatingParam);
}
