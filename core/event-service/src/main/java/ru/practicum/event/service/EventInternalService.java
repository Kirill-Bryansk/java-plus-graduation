package ru.practicum.event.service;

import ru.practicum.dto.event.EventForRequestDto;

/**
 * Внутренний сервис событий для межсервисного взаимодействия.
 * Используется rating-service для проверки существования событий
 * и получения базовых данных о событии.
 */
public interface EventInternalService {

    /**
     * Получить событие по ID для внутреннего использования.
     *
     * @param id идентификатор события
     * @return EventForRequestDto с данными события
     */
    EventForRequestDto getById(Long id);

    /**
     * Проверить, является ли пользователь инициатором события.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return true если пользователь — создатель события
     */
    boolean checkUserIsInitiator(long userId, long eventId);

    /**
     * Проверить существование события по ID.
     *
     * @param id идентификатор события
     * @throws NotFoundException если событие не найдено
     */
    void checkExistsById(long id);

}