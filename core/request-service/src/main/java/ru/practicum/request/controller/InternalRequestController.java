package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.contract.RequestOperations;
import ru.practicum.request.service.RequestInternalService;

import java.util.List;
import java.util.Map;

/**
 * Внутренний контроллер заявок для вызовов между сервисами (Feign).
 * Используется event-service для получения количества подтверждённых заявок
 * при отображении событий и поиске.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/requests")
@Slf4j
public class InternalRequestController implements RequestOperations {

    private final RequestInternalService requestService;

    /**
     * Получить количество подтверждённых заявок для списка событий.
     *
     * @param eventsIds список идентификаторов событий
     * @return Map<event_id, confirmed_count>
     */
    @Override
    @PostMapping("/count")
    public Map<Long, Integer> getCountConfirmedRequestsByEventIds(@RequestBody List<Long> eventsIds) {
        log.debug("POST /internal/requests/count: подсчёт подтверждённых заявок для eventIds={}", eventsIds);
        return requestService.getCountConfirmedRequestsByEventIds(eventsIds);
    }

    /**
     * Получить количество подтверждённых заявок для одного события.
     *
     * @param eventId идентификатор события
     * @return количество подтверждённых заявок
     */
    @Override
    @GetMapping("/count/{eventId}")
    public int getCountConfirmedRequestsByEventId(@PathVariable Long eventId) {
        log.debug("GET /internal/requests/count/{}: подсчёт подтверждённых заявок", eventId);
        return requestService.getCountConfirmedRequestsByEventId(eventId);
    }
}
