package ru.practicum.event.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;

import java.util.List;

/**
 * Сервис событий.
 * Предоставляет методы для управления событиями: создание, обновление,
 * удаление, поиск с фильтрацией для администратора, пользователей и публичного доступа.
 */
public interface EventService {

    /**
     * Создать новое событие от имени пользователя.
     * Проверяет, что дата события не ранее чем через 2 часа.
     *
     * @param newEvent данные нового события
     * @param userId   идентификатор пользователя-инициатора
     * @return EventFullDto созданного события
     * @throws ForbiddenException если дата события слишком близко
     */
    EventFullDto add(EventNewDto newEvent, long userId);

    /**
     * Получить список событий текущего пользователя с пагинацией.
     *
     * @param userId   идентификатор пользователя
     * @param pageable параметры пагинации
     * @return список EventShortDto
     */
    List<EventShortDto> getAllByUser(long userId, Pageable pageable);

    /**
     * Получить полное описание своего события по ID.
     *
     * @param eventId идентификатор события
     * @param userId  идентификатор пользователя
     * @return EventFullDto
     * @throws NotFoundException если событие не найдено или не принадлежит пользователю
     */
    EventFullDto getByIdPrivate(long eventId, long userId);

    /**
     * Обновить своё событие или изменить его статус.
     * Нельзя обновлять опубликованные события.
     *
     * @param userId      идентификатор пользователя
     * @param eventId     идентификатор события
     * @param eventUpdate данные для обновления
     * @return EventFullDto обновлённого события
     * @throws ConditionsNotMetException если событие опубликовано
     * @throws ForbiddenException если дата события слишком близко
     */
    EventFullDto updatePrivate(long userId, long eventId, EventUserUpdateDto eventUpdate);

    /**
     * Обновить событие или изменить его статус (администратор).
     * Может публиковать или отклонять события.
     *
     * @param eventId      идентификатор события
     * @param eventUpdate  данные для обновления
     * @return EventFullDto обновлённого события
     * @throws ConditionsNotMetException если состояние не позволяет операцию
     */
    EventFullDto updateAdmin(long eventId, EventAdminUpdateDto eventUpdate);

    /**
     * Получить список событий с фильтрацией и пагинацией (администратор).
     * Фильтры: пользователи, статусы, категории, диапазон дат.
     *
     * @param params параметры запроса
     * @return список EventFullDto
     */
    List<EventFullDto> getAllByAdmin(EventAdminParam params);

    /**
     * Получить список опубликованных событий с фильтрацией и пагинацией (публичный).
     * Фильтры: текстовый поиск, категории, платность, диапазон дат, доступность, сортировка.
     *
     * @param params параметры запроса
     * @return список EventShortDto
     */
    List<EventShortDto> getAllPublic(EventPublicParam params);

    /**
     * Получить полную информацию об опубликованном событии по ID (публичный).
     * Отправляет hit в stats-service для учёта просмотра.
     *
     * @param eventId идентификатор события
     * @param statDto данные для отправки в stats-service
     * @return EventFullDto
     * @throws NotFoundException если событие не найдено или не опубликовано
     */
    EventFullDto getByIdPublic(long eventId, long userId);

    /**
     * Преобразовать список сущностей Event в список EventShortDto.
     * Используется другими сервисами (например, compilation-service).
     *
     * @param events список сущностей Event
     * @return список EventShortDto
     */
    List<EventShortDto> getShortEvents(List<Event> events);

    /**
     * Получить список событий по списку ID.
     * Используется при созданиии подборок.
     *
     * @param eventsIds список идентификаторов событий
     * @return список сущностей Event
     */
    List<Event> getAllByIds(List<Long> eventsIds);

}
