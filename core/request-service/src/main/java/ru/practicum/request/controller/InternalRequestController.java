package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.contract.RequestOperations;
import ru.practicum.request.service.RequestInternalService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/requests")
@Slf4j
public class InternalRequestController implements RequestOperations {

    private final RequestInternalService requestService;

    @Override
    @PostMapping("/count")
    public Map<Long, Integer> getCountConfirmedRequestsByEventIds(@RequestBody List<Long> eventsIds) {
        log.debug("POST /internal/requests/count: подсчёт подтверждённых заявок для eventIds={}", eventsIds);
        return requestService.getCountConfirmedRequestsByEventIds(eventsIds);
    }

    @Override
    @GetMapping("/count/{eventId}")
    public int getCountConfirmedRequestsByEventId(@PathVariable Long eventId) {
        log.debug("GET /internal/requests/count/{}: подсчёт подтверждённых заявок", eventId);
        return requestService.getCountConfirmedRequestsByEventId(eventId);
    }
}
