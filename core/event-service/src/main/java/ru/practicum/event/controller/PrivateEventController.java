package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventNewDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventUserUpdateDto;
import ru.practicum.event.service.EventService;

import java.util.List;

/**
 * Приватный контроллер событий пользователя.
 * Предоставляет endpoints для управления событиями текущего пользователя:
 * создание, просмотр своих событий, обновление и отмена.
 */
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {

    private final EventService eventService;

    /**
     * Создать новое событие от имени пользователя.
     *
     * @param newEvent данные нового события
     * @param userId   идентификатор пользователя-инициатора
     * @return EventFullDto созданного события
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@RequestBody @Valid EventNewDto newEvent, @PathVariable long userId) {
        log.debug("POST: Запрос на создание события: userId={}, {}", userId, newEvent.getTitle());
        return eventService.add(newEvent, userId);
    }

    /**
     * Получить список событий текущего пользователя с пагинацией.
     *
     * @param from   номер первого элемента (по умолчанию 0)
     * @param size   количество элементов в выборке (по умолчанию 10)
     * @param userId идентификатор пользователя
     * @return список EventShortDto
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getAllByUser(@RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size, @PathVariable long userId) {
        log.debug("GET: Запрос на получение событий пользователя: userId={}, from={}, size={}", userId, from, size);
        return eventService.getAllByUser(userId, PageRequest.of(from, size));
    }

    /**
     * Получить полное описание своего события по ID.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return EventFullDto
     */
    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getById(@PathVariable long userId, @PathVariable long eventId) {
        log.debug("GET: Запрос на получение события: userId={}, eventId={}", userId, eventId);
        return eventService.getByIdPrivate(eventId, userId);
    }

    /**
     * Обновить своё событие или изменить его статус (отмена/отправка на модерацию).
     *
     * @param userId      идентификатор пользователя
     * @param eventId     идентификатор события
     * @param eventUpdate данные для обновления
     * @return EventFullDto обновлённого события
     */
    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto update(@PathVariable long userId, @PathVariable long eventId,
                               @RequestBody @Valid EventUserUpdateDto eventUpdate) {
        log.debug("PATCH: Запрос на обновление события: userId={}, eventId={}, {}", userId, eventId, eventUpdate);
        return eventService.updatePrivate(userId, eventId, eventUpdate);
    }
}
