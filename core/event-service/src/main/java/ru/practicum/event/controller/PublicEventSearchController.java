package ru.practicum.event.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.rating.EventSearchByRatingParam;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventSearchService;

import java.util.List;

/**
 * Публичный контроллер поиска событий по рейтингу.
 * Предоставляет endpoint для получения самых популярных событий
 * на основе лайков из rating-service.
 */
@Slf4j
@RestController
@RequestMapping("/events/top-liked")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PublicEventSearchController {
    EventSearchService eventSearchService;

    /**
     * Получить список самых популярных событий по количеству лайков.
     * Вызывает rating-service через Feign для получения рейтингов.
     *
     * @param limit максимальное количество событий (по умолчанию 10)
     * @return список EventShortDto, отсортированный по рейтингу
     */
    @GetMapping
    public List<EventShortDto> searchByRating(
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
        log.debug("GET: Запрос на поиск популярных событий: limit={}", limit);
        EventSearchByRatingParam param = new EventSearchByRatingParam();
        param.setLimit(limit);
        return eventSearchService.searchMostLikedEvents(param);
    }
}
