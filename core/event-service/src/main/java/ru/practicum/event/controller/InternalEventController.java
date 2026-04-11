package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.contract.EventOperations;
import ru.practicum.dto.event.EventForRequestDto;
import ru.practicum.event.service.EventInternalService;

@Slf4j
@RestController
@RequestMapping(path = "/internal/events")
@RequiredArgsConstructor
public class InternalEventController implements EventOperations {

    private final EventInternalService eventService;

    @Override
    @GetMapping("/{id}")
    public EventForRequestDto getById(@PathVariable Long id) {
        log.debug("GET /internal/events/{}: запрос события", id);
        return eventService.getById(id);
    }

    @Override
    @GetMapping("check/user/{userId}/event/{eventId}")
    public boolean checkUserIsInitiator(@PathVariable long userId, @PathVariable long eventId) {
        log.debug("GET /internal/events/check/user/{}/event/{}: проверка инициатора", userId, eventId);
        return eventService.checkUserIsInitiator(userId, eventId);
    }

    @Override
    @GetMapping("/exists/{id}")
    public void checkExistsById(long id) {
        log.debug("GET /internal/events/exists/{}: проверка существования события", id);
        eventService.checkExistsById(id);
    }
}
