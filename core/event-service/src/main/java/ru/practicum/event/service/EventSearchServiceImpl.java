package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.grpc.RatingClient;
import ru.practicum.dto.rating.EventSearchByRatingParam;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реализация сервиса поиска событий по рейтингу.
 * Вызывает rating-service через Feign для получения ID самых лайкнутых событий,
 * затем загружает полные данные о событиях и возвращает в нужном порядке.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventSearchServiceImpl implements EventSearchService {
    private final RatingClient ratingClient;
    private final EventService eventService;
    private final EventRepository eventRepository;

    /**
     * Найти самые популярные события по количеству лайков.
     * Вызывает rating-service через Feign для получения рейтингов.
     *
     * @param eventSearchByRatingParam параметры поиска (limit)
     * @return список EventShortDto, отсортированный по рейтингу
     */
    @Transactional
    @Override
    public List<EventShortDto> searchMostLikedEvents(EventSearchByRatingParam eventSearchByRatingParam) {
        log.info("Поиск популярных событий: limit={}", eventSearchByRatingParam.getLimit());
        List<Long> eventsIds = ratingClient.getMostLikedEventIds(eventSearchByRatingParam);
        List<Event> events = eventRepository.findAllByIdIn(eventsIds);

        Map<Long, Event> eventMap = events.stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        List<Event> orderedEvents = eventsIds.stream()
                .map(eventMap::get)
                .filter(Objects::nonNull)
                .toList();

        List<EventShortDto> result = eventService.getShortEvents(orderedEvents);
        log.info("Найдено {} популярных событий", result.size());
        return result;
    }
}
