package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.EventForRequestDto;
import ru.practicum.enums.event.EventState;
import ru.practicum.enums.request.RequestStatus;
import ru.practicum.errors.exceptions.ConditionsNotMetException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса заявок на участие в событиях.
 * Выполняет CRUD-операции с заявками, проверяет условия создания заявок
 * (опубликованность события, уникальность, лимит участников),
 * и управляет статусами заявок (подтверждение/отклонение) инициатором события.
 * Взаимодействует с event-service и user-service через Feign-клиенты.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    @Override
    public ParticipationRequestDto createParticipationRequest(long userId, long eventId) {
        log.info("Создание заявки на участие: userId={}, eventId={}", userId, eventId);

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConditionsNotMetException("Нельзя добавить повторный запрос на участие в событии");
        }

        EventForRequestDto event = eventClient.getById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Нельзя участвовать в неопубликованном событии");
        }

        if (event.getInitiatorId() == userId) {
            throw new ConditionsNotMetException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        checkParticipantLimit(event.getParticipantLimit(), getConfirmedRequests(eventId));

        userClient.checkUserExists(userId);

        Request saved = saveRequest(event, userId);
        ParticipationRequestDto dto = requestMapper.toDto(saved);
        log.info("Создана заявка id={}, статус={}, userId={}, eventId={}",
                dto.getId(), dto.getStatus(), userId, eventId);
        return dto;
    }

    @Override
    public List<ParticipationRequestDto> getAllByParticipantId(long userId) {
        log.info("Получение всех заявок пользователя userId={}", userId);
        List<Request> foundRequests = requestRepository.findAllByRequesterId(userId);
        List<ParticipationRequestDto> result = requestMapper.toDtoList(foundRequests);
        log.info("Найдено {} заявок для userId={}", result.size(), userId);
        return result;
    }

    @Override
    public List<ParticipationRequestDto> getAllByInitiatorIdAndEventId(long userId, long eventId) {
        log.info("Получение заявок на событие eventId={} от инициатора userId={}", eventId, userId);
        if (!eventClient.checkUserIsInitiator(userId, eventId)) {
            throw new ConditionsNotMetException("Пользователь не является владельцем события.");
        }
        List<Request> foundRequests = requestRepository.findAllByEventId(eventId);
        List<ParticipationRequestDto> result = requestMapper.toDtoList(foundRequests);
        log.info("Найдено {} заявок на событие eventId={}", result.size(), eventId);
        return result;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeEventRequestsStatusByInitiator(EventRequestStatusUpdateRequest updateRequest, long userId, long eventId) {
        log.info("Изменение статусов заявок: userId={}, eventId={}, requestIds={}, новый статус={}",
                userId, eventId, updateRequest.getRequestIds(), updateRequest.getStatus());

        EventForRequestDto event = eventClient.getById(eventId);
        if (event.getInitiatorId() != userId) {
            throw new ConditionsNotMetException("Пользователь не является владельцем события.");
        }
        List<Long> requestIds = updateRequest.getRequestIds();
        List<Request> foundRequests = requestRepository.findAllById(requestIds);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (Request request : foundRequests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConditionsNotMetException("Заявка должна находиться в ожидании");
            }
        }

        switch (updateRequest.getStatus()) {
            case CONFIRMED -> handleConfirmedRequests(event, foundRequests, result, confirmed, rejected);
            case REJECTED -> handleRejectedRequests(foundRequests, rejected);
        }

        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        log.info("Результат изменения статусов: подтверждено={}, отклонено={}",
                confirmed.size(), rejected.size());
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelParticipantRequest(long userId, long requestId) {
        log.info("Отмена заявки: userId={}, requestId={}", userId, requestId);
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException(String.format("Запрос на участие в событии с id запроса=%d не найден", requestId))
        );

        Long requesterId = request.getRequesterId();
        if (!requesterId.equals(userId)) {
            throw new ConditionsNotMetException("Пользователь не является участником в запросе на участие в событии");
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        ParticipationRequestDto dto = requestMapper.toDto(request);
        log.info("Заявка requestId={} отменена, новый статус={}", requestId, dto.getStatus());
        return dto;
    }

    @Transactional
    private Request saveRequest(EventForRequestDto event, long userId) {
        RequestStatus status = RequestStatus.PENDING;
        if (event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        } else if (!event.getRequestModeration()) {
            status = RequestStatus.CONFIRMED;
        }
        log.debug("Сохранение заявки: userId={}, eventId={}, статус={}", userId, event.getId(), status);

        Request request = Request.builder()
                .eventId(event.getId())
                .requesterId(userId)
                .status(status)
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .build();

        return requestRepository.save(request);
    }

    private void checkParticipantLimit(int participantLimit, int confirmedRequests) {
        if (confirmedRequests >= participantLimit && participantLimit != 0) {
            throw new ConditionsNotMetException("У события заполнен лимит участников");
        }
    }

    private int getConfirmedRequests(long eventId) {
        return requestRepository.findCountOfConfirmedRequestsByEventId(eventId);
    }

    private void updateStatus(RequestStatus status, List<Long> ids) {
        log.debug("Обновление статуса {} для заявок {}", status, ids);
        requestRepository.updateStatus(status, ids);
    }

    private void handleConfirmedRequests(EventForRequestDto event, List<Request> foundRequests,
                                         EventRequestStatusUpdateResult result,
                                         List<ParticipationRequestDto> confirmed,
                                         List<ParticipationRequestDto> rejected) {
        int confirmedRequests = getConfirmedRequests(event.getId());
        int participantLimit = event.getParticipantLimit();
        log.debug("Обработка подтверждения заявок: текущее количество подтверждённых={}, лимит={}",
                confirmedRequests, participantLimit);

        if (participantLimit == 0 || !event.getRequestModeration()) {
            log.debug("Лимит 0 или модерация отключена: подтверждаем все заявки без проверок");
            result.setConfirmedRequests(requestMapper.toDtoList(foundRequests));
            return;
        }
        checkParticipantLimit(participantLimit, confirmedRequests);

        for (Request request : foundRequests) {
            if (confirmedRequests >= participantLimit) {
                rejected.add(requestMapper.toDto(request));
                log.debug("Заявка id={} отклонена из-за достижения лимита", request.getId());
                continue;
            }
            request.setStatus(RequestStatus.CONFIRMED);
            confirmed.add(requestMapper.toDto(request));
            ++confirmedRequests;
            log.debug("Заявка id={} подтверждена", request.getId());
        }
        List<Long> confirmedRequestIds = confirmed.stream().map(ParticipationRequestDto::getId).toList();
        updateStatus(RequestStatus.CONFIRMED, confirmedRequestIds);
        log.debug("Подтверждено {} заявок, отклонено {} (лимит)", confirmed.size(), rejected.size());
    }

    private void handleRejectedRequests(List<Request> foundRequests, List<ParticipationRequestDto> rejected) {
        log.debug("Обработка отклонения заявок: всего заявок={}", foundRequests.size());
        for (Request request : foundRequests) {
            request.setStatus(RequestStatus.REJECTED);
            rejected.add(requestMapper.toDto(request));
            log.debug("Заявка id={} отклонена", request.getId());
        }
        List<Long> rejectedRequestIds = rejected.stream().map(ParticipationRequestDto::getId).toList();
        updateStatus(RequestStatus.REJECTED, rejectedRequestIds);
        log.debug("Отклонено {} заявок", rejected.size());
    }
}