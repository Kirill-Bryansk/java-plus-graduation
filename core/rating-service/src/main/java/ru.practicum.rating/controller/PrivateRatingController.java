package ru.practicum.rating.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.rating.dto.NewRatingDto;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.UpdateRatingDto;
import ru.practicum.rating.service.RatingService;

/**
 * Приватный контроллер оценок событий.
 * Предоставляет endpoints для пользователей: создание, обновление и удаление оценок (LIKE/DISLIKE).
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events/{eventId}/ratings")
public class PrivateRatingController {
    private final RatingService ratingService;

    /**
     * Создать оценку события (LIKE или DISLIKE).
     * Проверяет существование пользователя и события через Feign-клиенты.
     *
     * @param userId        идентификатор пользователя
     * @param eventId       идентификатор события
     * @param newRatingDto  данные оценки (mark)
     * @return RatingDto созданной оценки
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto add(@PathVariable("userId") long userId,
                         @PathVariable("eventId") long eventId,
                         @RequestBody @Valid NewRatingDto newRatingDto) {
        log.debug("POST: Запрос на создание оценки: userId={}, eventId={}, {}", userId, eventId, newRatingDto);
        return ratingService.create(userId, eventId, newRatingDto);
    }

    /**
     * Обновить оценку события (изменить LIKE на DISLIKE или наоборот).
     *
     * @param userId          идентификатор пользователя
     * @param eventId         идентификатор события
     * @param ratingId        идентификатор оценки
     * @param updateRatingDto новые данные оценки (mark)
     * @return RatingDto обновлённой оценки
     */
    @PatchMapping("/{ratingId}")
    @ResponseStatus(HttpStatus.OK)
    public RatingDto update(@PathVariable("userId") long userId,
                            @PathVariable("eventId") long eventId,
                            @PathVariable("ratingId") long ratingId,
                            @RequestBody @Valid UpdateRatingDto updateRatingDto) {
        log.debug("PATCH: Запрос на обновление оценки: userId={}, eventId={}, ratingId={}, {}",
                userId, eventId, ratingId, updateRatingDto);
        return ratingService.update(userId, eventId, ratingId, updateRatingDto);
    }

    /**
     * Удалить оценку события.
     *
     * @param userId   идентификатор пользователя
     * @param eventId  идентификатор события
     * @param ratingId идентификатор оценки
     */
    @DeleteMapping("/{ratingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable("userId") long userId,
                       @PathVariable("eventId") long eventId,
                       @PathVariable("ratingId") long ratingId) {
        log.debug("DELETE: Запрос на удаление оценки: userId={}, eventId={}, ratingId={}", userId, eventId, ratingId);
        ratingService.delete(userId, eventId, ratingId);
    }
}
