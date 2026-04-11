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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateRequestController {
    private final RequestService requestService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable(name = "userId") @Positive long userId,
                                          @RequestParam(name = "eventId") @Positive long eventId) {
        log.debug("POST /users/{}/requests: запрос на участие, eventId={}", userId, eventId);
        return requestService.createParticipationRequest(userId, eventId);
    }

    @GetMapping("/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllByParticipantId(@PathVariable(name = "userId") @Positive long userId) {
        log.debug("GET /users/{}/requests: запрос всех заявок пользователя", userId);
        return requestService.getAllByParticipantId(userId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelParticipantRequest(@PathVariable(name = "userId") @Positive long userId,
                                                            @PathVariable(name = "requestId") @Positive long requestId) {
        log.debug("PATCH /users/{}/requests/{}/cancel: отмена заявки", userId, requestId);
        return requestService.cancelParticipantRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllEventsOfInitiator(@PathVariable(name = "userId") @Positive long userId,
                                                                 @PathVariable(name = "eventId") @Positive long eventId) {
        log.debug("GET /users/{}/events/{}/requests: запрос заявок на событие", userId, eventId);
        return requestService.getAllByInitiatorIdAndEventId(userId, eventId);
    }

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