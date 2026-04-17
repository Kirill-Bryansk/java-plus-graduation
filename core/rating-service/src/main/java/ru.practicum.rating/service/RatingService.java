package ru.practicum.rating.service;

import ru.practicum.rating.dto.NewRatingDto;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.UpdateRatingDto;

/**
 * Сервис оценок событий.
 * Предоставляет методы для создания, обновления и удаления оценок (LIKE/DISLIKE).
 */
public interface RatingService {

    /**
     * Создать оценку события.
     * Проверяет существование пользователя и события через Feign-клиенты.
     *
     * @param userId       идентификатор пользователя
     * @param eventId      идентификатор события
     * @param newRatingDto данные оценки (mark)
     * @return RatingDto созданной оценки
     * @throws ConditionsNotMetException если пользователь уже оценивал это событие
     */
    RatingDto create(long userId, long eventId, NewRatingDto newRatingDto);

    /**
     * Обновить оценку события (изменить LIKE на DISLIKE или наоборот).
     *
     * @param userId          идентификатор пользователя
     * @param eventId         идентификатор события
     * @param ratingId        идентификатор оценки
     * @param updateRatingDto новые данные оценки (mark)
     * @return RatingDto обновлённой оценки
     * @throws NotFoundException если оценка не найдена
     * @throws ConditionsNotMetException если оценка не принадлежит пользователю
     */
    RatingDto update(long userId, long eventId, long ratingId, UpdateRatingDto updateRatingDto);

    /**
     * Удалить оценку события.
     *
     * @param userId   идентификатор пользователя
     * @param eventId  идентификатор события
     * @param ratingId идентификатор оценки
     * @throws NotFoundException если оценка не найдена
     * @throws ConditionsNotMetException если оценка не принадлежит пользователю
     */
    void delete(long userId, long eventId, long ratingId);
}
