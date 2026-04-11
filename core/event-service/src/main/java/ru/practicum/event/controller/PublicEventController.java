package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatsClientService;
import ru.practicum.dto.StatDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventPublicParam;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Публичный контроллер событий.
 * Предоставляет endpoints для просмотра опубликованных событий:
 * поиск с фильтрацией и получение детальной информации.
 * Автоматически отправляет запросы в stats-service для учёта просмотров.
 */
@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {

    private final EventService eventService;
    private final StatsClientService statsClient;

    /**
     * Получить список опубликованных событий с фильтрацией и пагинацией.
     * Фильтры: текстовый поиск, категории, платность, диапазон дат, доступность, сортировка.
     * Отправляет hit в stats-service для учёта просмотра.
     *
     * @param text          текст для поиска (необязательный)
     * @param categories    список ID категорий (необязательный)
     * @param paid          платность (true/false, необязательный)
     * @param rangeStart    начало диапазона дат (необязательный)
     * @param rangeEnd      конец диапазона дат (необязательный)
     * @param onlyAvailable только доступные события (необязательный)
     * @param sort          тип сортировки (необязательный)
     * @param from          номер первого элемента (по умолчанию 0)
     * @param size          количество элементов в выборке (по умолчанию 10)
     * @param request       HTTP-запрос для получения IP и URI
     * @return список EventShortDto
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
                                      @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        log.debug("GET: Запрос на получение публичных событий: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        String clientIp = request.getRemoteAddr();
        String requestUri = request.getRequestURI();
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
        List<EventShortDto> events = eventService.getAllPublic(eventPublicParam);
        statsClient.hit(new StatDto("ewm-main-service", requestUri, clientIp, LocalDateTime.now()));
        return events;
    }

    /**
     * Получить полную информацию об опубликованном событии по ID.
     * Отправляет hit в stats-service для учёта просмотра.
     *
     * @param id      идентификатор события
     * @param request HTTP-запрос для получения IP и URI
     * @return EventFullDto
     */
    @GetMapping("/{id}")
    public EventFullDto getById(@PathVariable long id, HttpServletRequest request) {
        log.debug("GET: Запрос на получение публичного события с id: {}", id);
        String clientIp = request.getRemoteAddr();
        String requestUri = request.getRequestURI();
        return eventService.getByIdPublic(id, new StatDto("ewm-main-service", requestUri, clientIp,
                LocalDateTime.now()));
    }
}
