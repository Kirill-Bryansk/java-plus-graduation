package ru.practicum.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

/**
 * Приватный контроллер заявок на участие в событиях.
 * Предоставляет endpoints для пользователей: создание заявок на участие,
 * просмотр своих заявок, отмена заявок, а также управление заявками
 * для инициаторов событий (просмотр, подтверждение, отклонение).
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateRequestController {
    private final RequestService requestService;

    /**
     * Создать заявку на участие в событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return ParticipationRequestDto созданной заявки
     */
    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable(name = "userId") @Positive long userId,
                                          @RequestParam(name = "eventId") @Positive long eventId) {
        log.debug("POST /users/{}/requests: запрос на участие, eventId={}", userId, eventId);
        return requestService.createParticipationRequest(userId, eventId);
    }

    /**
     * Получить все заявки пользователя на участие в событиях.
     *
     * @param userId идентификатор пользователя
     * @return список ParticipationRequestDto
     */
    @GetMapping("/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllByParticipantId(@PathVariable(name = "userId") @Positive long userId) {
        log.debug("GET /users/{}/requests: запрос всех заявок пользователя", userId);
        return requestService.getAllByParticipantId(userId);
    }

    /**
     * Отменить заявку на участие в событии.
     *
     * @param userId   идентификатор пользователя
     * @param requestId идентификатор заявки
     * @return ParticipationRequestDto отменённой заявки
     */
    @PatchMapping("/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelParticipantRequest(@PathVariable(name = "userId") @Positive long userId,
                                                            @PathVariable(name = "requestId") @Positive long requestId) {
        log.debug("PATCH /users/{}/requests/{}/cancel: отмена заявки", userId, requestId);
        return requestService.cancelParticipantRequest(userId, requestId);
    }

    /**
     * Получить все заявки на участие в событии (только для инициатора события).
     *
     * @param userId  идентификатор пользователя-инициатора
     * @param eventId идентификатор события
     * @return список ParticipationRequestDto
     */
    @GetMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllEventsOfInitiator(@PathVariable(name = "userId") @Positive long userId,
                                                                 @PathVariable(name = "eventId") @Positive long eventId) {
        log.debug("GET /users/{}/events/{}/requests: запрос заявок на событие", userId, eventId);
        return requestService.getAllByInitiatorIdAndEventId(userId, eventId);
    }

    /**
     * Изменить статус заявок на участие в событии (подтвердить/отклонить).
     * Только для инициатора события.
     *
     * @param userId        идентификатор пользователя-инициатора
     * @param eventId       идентификатор события
     * @param updateRequest данные для обновления (список ID заявок и новый статус)
     * @return EventRequestStatusUpdateResult с результатами подтверждения и отклонения
     */
    @PatchMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult changeRequestsStatus(@PathVariable(name = "userId") @Positive long userId,
                                                               @PathVariable(name = "eventId") @Positive long eventId,
                                                               @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        log.debug("PATCH /users/{}/events/{}/requests: изменение статусов заявок {} -> {}",
                userId, eventId, updateRequest.getRequestIds(), updateRequest.getStatus());
        return requestService.changeEventRequestsStatusByInitiator(updateRequest, userId, eventId);
    }
}