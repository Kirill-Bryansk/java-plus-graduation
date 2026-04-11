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

/**
 * Внутренний контроллер событий для вызовов между сервисами (Feign).
 * Используется rating-service для проверки существования событий
 * и получения данных о событии.
 */
@Slf4j
@RestController
@RequestMapping(path = "/internal/events")
@RequiredArgsConstructor
public class InternalEventController implements EventOperations {

    private final EventInternalService eventService;

    /**
     * Получить событие по ID для внутреннего использования.
     *
     * @param id идентификатор события
     * @return EventForRequestDto с данными события
     */
    @Override
    @GetMapping("/{id}")
    public EventForRequestDto getById(@PathVariable Long id) {
        log.debug("GET /internal/events/{}: запрос события", id);
        return eventService.getById(id);
    }

    /**
     * Проверить, является ли пользователь инициатором события.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return true если пользователь — создатель события
     */
    @Override
    @GetMapping("check/user/{userId}/event/{eventId}")
    public boolean checkUserIsInitiator(@PathVariable long userId, @PathVariable long eventId) {
        log.debug("GET /internal/events/check/user/{}/event/{}: проверка инициатора", userId, eventId);
        return eventService.checkUserIsInitiator(userId, eventId);
    }

    /**
     * Проверить существование события по ID.
     * Возвращает 200 если событие найдено, 404 если нет.
     *
     * @param id идентификатор события
     * @throws NotFoundException если событие не найдено
     */
    @Override
    @GetMapping("/exists/{id}")
    public void checkExistsById(long id) {
        log.debug("GET /internal/events/exists/{}: проверка существования события", id);
        eventService.checkExistsById(id);
    }
}
