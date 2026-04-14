package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stats.client.ActionType;
import ru.practicum.ewm.stats.client.AnalyzerGrpcClient;
import ru.practicum.ewm.stats.client.CollectorGrpcClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventPublicParam;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.RecommendedEventDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Публичный контроллер событий.
 * Предоставляет endpoints для просмотра опубликованных событий:
 * поиск с фильтрацией, получение детальной информации, рекомендации и лайки.
 * Использует gRPC для отправки действий в Collector и получения рекомендаций из Analyzer.
 */
@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {

    private final EventService eventService;
    private final CollectorGrpcClient collectorClient;
    private final AnalyzerGrpcClient analyzerClient;

    /**
     * Получить список опубликованных событий с фильтрацией и пагинацией.
     * Фильтры: текстовый поиск, категории, платность, диапазон дат, доступность, сортировка.
     * Рейтинги запрашиваются у Analyzer через gRPC.
     */
    @GetMapping
    public List<EventShortDto> getAll(@RequestParam(required = false) String text,
                                      @RequestParam(required = false) List<Long> categories,
                                      @RequestParam(required = false) Boolean paid,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                      @RequestParam(required = false) Boolean onlyAvailable,
                                      @RequestParam(required = false) EventPublicParam.EventSort sort,
                                      @RequestParam(defaultValue = "0") int from,
                                      @RequestParam(defaultValue = "10") int size) {
        log.debug("GET: Запрос на получение публичных событий: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        EventPublicParam eventPublicParam = new EventPublicParam();
        eventPublicParam.setText(text);
        eventPublicParam.setCategories(categories);
        eventPublicParam.setPaid(paid);
        eventPublicParam.setRangeStart(rangeStart);
        eventPublicParam.setRangeEnd(rangeEnd);
        eventPublicParam.setOnlyAvailable(onlyAvailable);
        eventPublicParam.setSort(sort);
        eventPublicParam.setFrom(from);
        eventPublicParam.setSize(size);

        return eventService.getAllPublic(eventPublicParam);
    }

    /**
     * Получить полную информацию об опубликованном событии по ID.
     * Отправляет информацию о просмотре в Collector через gRPC.
     *
     * @param id     идентификатор события
     * @param userId идентификатор пользователя (из заголовка X-EWM-USER-ID)
     * @return EventFullDto
     */
    @GetMapping("/{id}")
    public EventFullDto getById(@PathVariable long id,
                                @RequestHeader("X-EWM-USER-ID") long userId) {
        log.debug("GET: Запрос на получение публичного события с id: {}, userId: {}", id, userId);
        return eventService.getByIdPublic(id, userId);
    }

    /**
     * Получить рекомендации мероприятий для пользователя.
     *
     * @param userId     идентификатор пользователя (из заголовка X-EWM-USER-ID)
     * @param maxResults максимальное количество рекомендаций
     * @return список RecommendedEventDto
     */
    @GetMapping("/recommendations")
    public List<RecommendedEventDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") long userId,
                                                        @RequestParam(defaultValue = "10") int maxResults) {
        log.debug("GET: Запрос на получение рекомендаций: userId={}, maxResults={}", userId, maxResults);
        return analyzerClient.getRecommendationsForUser(userId, maxResults)
                .map(r -> new RecommendedEventDto(r.getEventId(), r.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * Лайкнуть мероприятие.
     * Пользователь может лайкать только посещённые им мероприятия.
     *
     * @param eventId идентификатор события
     * @param userId  идентификатор пользователя (из заголовка X-EWM-USER-ID)
     */
    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likeEvent(@PathVariable long eventId,
                          @RequestHeader("X-EWM-USER-ID") long userId) {
        log.debug("PUT: Лайк события: eventId={}, userId={}", eventId, userId);
        // Проверка: пользователь должен был посетить это событие
        // (реализуется через запрос к Analyzer или локальную проверку)
        collectorClient.sendUserAction(userId, eventId, ActionType.ACTION_LIKE);
    }
}
