package ru.practicum.event.service;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.ewm.stats.client.ActionType;
import ru.practicum.ewm.stats.client.AnalyzerGrpcClient;
import ru.practicum.ewm.stats.client.CollectorGrpcClient;
import ru.practicum.grpc.RequestClient;
import ru.practicum.grpc.UserClient;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enums.event.EventState;
import ru.practicum.errors.exceptions.ConditionsNotMetException;
import ru.practicum.errors.exceptions.ForbiddenException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.errors.exceptions.ValidationException;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реализация сервиса событий.
 * Выполняет CRUD-операции с событиями, фильтрацию через QueryDSL,
 * взаимодействие со stats-service для учёта просмотров,
 * с rating-service для получения рейтингов,
 * и с user-service для получения данных пользователей.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final EventMapper eventMapper;
    private final UserClient userClient;
    private final RequestClient requestClient;
    // gRPC-клиенты для recommendation-сервисов
    private final CollectorGrpcClient collectorClient;
    private final AnalyzerGrpcClient analyzerClient;

    @Override
    @Transactional
    public EventFullDto add(EventNewDto newEvent, long userId) {
        log.info("Создание события: userId={}, annotation={}", userId, newEvent.getAnnotation());
        LocalDateTime eventDate = newEvent.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ForbiddenException("Начало события ранее, чем через два часа: " + eventDate);
        }
        UserShortDto user = userClient.getById(userId);
        Event event = saveEvent(newEvent, userId);
        EventFullDto result = eventMapper.toFullDto(event, user);
        log.info("Событие создано с id={}, состояние={}", result.getId(), result.getState());
        return result;
    }

    @Override
    public List<EventShortDto> getAllByUser(long userId, Pageable pageable) {
        log.info("Получение событий пользователя: userId={}, from={}, size={}", userId, pageable.getPageNumber(), pageable.getPageSize());
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        events = applyConfirmedRequestsToEvents(events);
        List<EventShortDto> result = mapToShortDtos(events);
        log.info("Получено {} событий для userId={}", result.size(), userId);
        return result;
    }

    @Override
    public EventFullDto getByIdPrivate(long eventId, long userId) {
        log.info("Получение события (приватное): eventId={}, userId={}", eventId, userId);
        Event event = findByIdAndInitiatorId(eventId, userId);
        applyConfirmedRequestsToEvent(event);
        UserShortDto user = userClient.getById(userId);
        EventFullDto result = eventMapper.toFullDto(event, user);
        log.info("Событие найдено: id={}, состояние={}", result.getId(), result.getState());
        return result;
    }

    @Override
    @Transactional
    public EventFullDto updatePrivate(long userId, long eventId, EventUserUpdateDto eventUpdate) {
        log.info("Обновление события (приватное): userId={}, eventId={}, stateAction={}",
                userId, eventId, eventUpdate.getStateAction());
        Event event = findByIdAndInitiatorId(eventId, userId);

        boolean isPublished = event.getState() == EventState.PUBLISHED;
        if (isPublished) {
            throw new ConditionsNotMetException("Нельзя обновить опубликованное событие");
        }

        Long categoryId = eventUpdate.getCategory();
        if (categoryId != null) {
            Category category = categoryService.findByIdOrThrow(categoryId);
            event.setCategory(category);
        }

        LocalDateTime eventDate = eventUpdate.getEventDate();
        if (eventDate != null) {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ForbiddenException("Начало события должно быть не ранее, чем через два часа: " + eventDate);
            }
            event.setEventDate(eventDate);
        }

        EventUserUpdateDto.StateAction stateAction = eventUpdate.getStateAction();
        if (stateAction != null) {
            switch (stateAction) {
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            }
        }

        Event updated = eventMapper.toEventFromEventUserUpdateDto(eventUpdate, event);
        updated = eventRepository.save(updated);
        UserShortDto user = userClient.getById(updated.getInitiatorId());
        EventFullDto result = eventMapper.toFullDto(updated, user);
        log.info("Событие обновлено: id={}, новое состояние={}", result.getId(), result.getState());
        return result;
    }

    @Override
    @Transactional
    public EventFullDto updateAdmin(long eventId, EventAdminUpdateDto eventUpdate) {
        log.info("Обновление события (админ): eventId={}, stateAction={}", eventId, eventUpdate.getStateAction());
        Event event = findById(eventId);

        EventAdminUpdateDto.StateAction stateAction = eventUpdate.getStateAction();
        if (stateAction != null) {
            switch (stateAction) {
                case PUBLISH_EVENT -> handlePublishEvent(event, eventUpdate);
                case REJECT_EVENT -> handleRejectEvent(event);
            }
        }

        Long categoryId = eventUpdate.getCategory();
        if (categoryId != null) {
            Category category = categoryService.findByIdOrThrow(categoryId);
            event.setCategory(category);
        }

        Event updated = eventMapper.toEventFromEventAdminUpdateDto(eventUpdate, event);
        UserShortDto user = userClient.getById(updated.getInitiatorId());
        updated = eventRepository.save(updated);
        EventFullDto result = eventMapper.toFullDto(updated, user);
        log.info("Событие обновлено (админ): id={}, новое состояние={}", result.getId(), result.getState());
        return result;
    }

    @Override
    public List<EventFullDto> getAllByAdmin(EventAdminParam params) {
        log.info("Получение событий (админ): users={}, states={}, categories={}, rangeStart={}, rangeEnd={}",
                params.getUsers(), params.getStates(), params.getCategories(), params.getRangeStart(), params.getRangeEnd());
        List<Long> users = params.getUsers();
        BooleanExpression byUsers = (users != null && !users.isEmpty())
                ? QEvent.event.initiatorId.in(users) : null;

        List<EventState> states = params.getStates();
        BooleanExpression byStates = (states != null && !states.isEmpty())
                ? QEvent.event.state.in(states) : null;

        List<Long> categories = params.getCategories();
        BooleanExpression byCategories = (categories != null && !categories.isEmpty())
                ? QEvent.event.category.id.in(params.getCategories()) : null;

        BooleanExpression byEventDate = (params.getRangeStart() != null && params.getRangeEnd() != null)
                ? QEvent.event.eventDate.between(params.getRangeStart(), params.getRangeEnd()) : null;

        Predicate predicate = ExpressionUtils.allOf(byUsers, byStates, byCategories, byEventDate);

        List<Event> events = (predicate != null)
                ? eventRepository.findAll(predicate, params.getPageable()).toList()
                : eventRepository.findAll(params.getPageable()).toList();

        events = applyConfirmedRequestsToEvents(events);

        List<EventFullDto> result = eventMapper.toEventFullDtoList(events);
        log.info("Получено {} событий (админ)", result.size());
        return result;
    }

    @Override
    public List<EventShortDto> getAllPublic(EventPublicParam params) {
        log.info("Получение публичных событий: text={}, categories={}, paid={}, onlyAvailable={}, sort={}",
                params.getText(), params.getCategories(), params.getPaid(), params.getOnlyAvailable(), params.getSort());
        if (params.getRangeEnd() != null && params.getRangeStart() != null &&
                params.getRangeEnd().isBefore(params.getRangeStart())) {
            throw new ValidationException("Параметр rangeEnd должен быть позже rangeStart");
        }
        BooleanExpression byState = QEvent.event.state.eq(EventState.PUBLISHED);

        String text = params.getText();
        BooleanExpression byText = (text != null && !text.isEmpty()) ?
                QEvent.event.description.containsIgnoreCase(text)
                        .or(QEvent.event.annotation.containsIgnoreCase(text)) : null;

        BooleanExpression byEventDate = (params.getRangeStart() != null && params.getRangeEnd() != null)
                ? QEvent.event.eventDate.between(params.getRangeStart(), params.getRangeEnd())
                : QEvent.event.eventDate.after(LocalDateTime.now());

        List<Long> categories = params.getCategories();
        BooleanExpression byCategories = (categories != null && !categories.isEmpty())
                ? QEvent.event.category.id.in(params.getCategories()) : null;

        Boolean paid = params.getPaid();
        BooleanExpression byPaid = (paid != null) ? QEvent.event.paid.eq(paid) : null;

        Predicate predicate = ExpressionUtils.allOf(byState, byText, byPaid, byCategories, byEventDate);

        Pageable pageable = toPageable(params.getSort(), params.getFrom(), params.getSize());

        List<Event> events = (predicate != null)
                ? eventRepository.findAll(predicate, pageable).toList()
                : eventRepository.findAll(pageable).toList();

        events = applyConfirmedRequestsToEvents(events);

        if (params.getOnlyAvailable() != null && params.getOnlyAvailable()) {
            events = filterByAvailability(events);
        }

        List<EventShortDto> result = mapToShortDtos(events);

        // Запрашиваем рейтинги через gRPC Analyzer
        applyRatings(events, result);

        log.info("Получено {} публичных событий", result.size());
        return result;
    }

    @Override
    public EventFullDto getByIdPublic(long eventId, long userId) {
        log.info("Получение публичного события: eventId={}, userId={}", eventId, userId);
        Event event = findById(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие с id: " + eventId + " не найдено");
        }
        applyConfirmedRequestsToEvent(event);

        // Отправляем просмотр через gRPC Collector
        collectorClient.sendUserAction(userId, eventId, ActionType.ACTION_VIEW);

        UserShortDto user = userClient.getById(event.getInitiatorId());
        EventFullDto eventFullDto = eventMapper.toFullDto(event, user);

        // Запрашиваем рейтинг через gRPC Analyzer
        analyzerClient.getInteractionsCount(List.of(eventId))
                .findFirst()
                .ifPresent(r -> {
                    event.setRating(r.getScore());
                    eventFullDto.setRating(r.getScore());
                });

        log.info("Публичное событие возвращено: id={}, rating={}", eventFullDto.getId(), eventFullDto.getRating());
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getShortEvents(List<Event> events) {
        return mapToShortDtos(events);
    }

    @Override
    public List<Event> getAllByIds(List<Long> eventsIds) {
        return eventRepository.findAllByIdIn(eventsIds);
    }

    private Event saveEvent(EventNewDto newEvent, long userId) {
        long categoryId = newEvent.getCategory();
        Category category = categoryService.findByIdOrThrow(categoryId);
        Event event = eventMapper.toEvent(newEvent, category, userId);
        return eventRepository.save(event);
    }

    private Event findById(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id: " + eventId + " не существует"));
    }

    private Event findByIdAndInitiatorId(long eventId, long initiatorId) {
        return eventRepository.findByIdAndInitiatorId(eventId, initiatorId).orElseThrow(() ->
                new NotFoundException("Событие с id: " + eventId + " не существует"));
    }

    private Pageable toPageable(EventPublicParam.EventSort eventSort, int from, int size) {
        Sort sort = eventSort == null ?
                Sort.by(EventPublicParam.EventSort.EVENT_DATE.getField())
                : Sort.by(Sort.Direction.DESC, eventSort.getField());
        return PageRequest.of(from, size, sort);
    }

    private void handlePublishEvent(Event event, EventAdminUpdateDto eventUpdate) {
        if (!event.getState().equals(EventState.PENDING)) {
            throw new ConditionsNotMetException("Нельзя опубликовать событие, не находящееся в состоянии ожидания");
        }
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
        LocalDateTime eventDate = eventUpdate.getEventDate();
        if (eventDate != null) {
            if (eventDate.isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConditionsNotMetException(
                        "Начало события должно быть не ранее, чем через час от даты публикации: " + eventDate);
            }
            event.setEventDate(eventDate);
        }
    }

    private void handleRejectEvent(Event event) {
        if (!event.getState().equals(EventState.PENDING)) {
            throw new ConditionsNotMetException("Нельзя отклонить опубликованное событие");
        }
        event.setState(EventState.CANCELED);
    }

    private List<Event> applyConfirmedRequestsToEvents(List<Event> events) {
        List<Long> eventsIds = events.stream().map(Event::getId).toList();
        Map<Long, Integer> requestsByEventIds = requestClient.getCountConfirmedRequestsByEventIds(eventsIds);

        return events.stream().peek(event ->
                event.setConfirmedRequests(requestsByEventIds.getOrDefault(event.getId(), 0))
        ).collect(Collectors.toList());
    }

    private void applyConfirmedRequestsToEvent(Event event) {
        int confirmed = requestClient.getCountConfirmedRequestsByEventId(event.getId());
        event.setConfirmedRequests(confirmed);
    }

    private List<Event> filterByAvailability(List<Event> events) {
        return events.stream()
                .filter(event -> event.getConfirmedRequests() < event.getParticipantLimit())
                .collect(Collectors.toList());
    }

    private List<EventShortDto> mapToShortDtos(List<Event> events) {
        Set<Long> usersIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        Map<Long, UserShortDto> usersByIds = userClient.getAllUsersByIds(new ArrayList<>(usersIds));
        List<EventShortDto> eventShortDtos = eventMapper.toEventShortDtoList(events, usersByIds);
        return eventShortDtos;
    }

    /**
     * Запросить рейтинги мероприятий через gRPC Analyzer и применить их к DTO.
     */
    private void applyRatings(List<Event> events, List<EventShortDto> dtos) {
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        if (eventIds.isEmpty()) return;

        Map<Long, Double> ratingById = analyzerClient.getInteractionsCount(eventIds)
                .collect(Collectors.toMap(
                        r -> r.getEventId(),
                        r -> r.getScore()
                ));

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            EventShortDto dto = dtos.get(i);
            double rating = ratingById.getOrDefault(event.getId(), 0.0);
            event.setRating(rating);
            dto.setRating(rating);
        }
    }
}
