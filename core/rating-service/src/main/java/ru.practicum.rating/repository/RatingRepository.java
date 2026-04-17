package ru.practicum.rating.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.rating.model.Rating;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий оценок событий.
 * Предоставляет методы поиска оценок по пользователю и событию,
 * а также запрос самых популярных событий по количеству лайков.
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Проверить, оценивал ли пользователь данное событие.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return true если оценка уже существует
     */
    boolean existsByUserIdAndEventId(long userId, long eventId);

    /**
     * Найти оценку по ID и ID пользователя (для проверки прав доступа).
     *
     * @param ratingId идентификатор оценки
     * @param userId   идентификатор пользователя
     * @return Optional с найденной оценкой
     */
    Optional<Rating> findByIdAndUserId(long ratingId, long userId);

    /**
     * Найти самые популярные события по количеству лайков.
     * Группирует оценки по eventId, фильтрует только LIKE, сортирует по убыванию.
     *
     * @param pageable параметры пагинации
     * @return список ID событий, отсортированный по количеству лайков
     */
    @Query("SELECT r.eventId " +
            "FROM Rating r " +
            "WHERE r.mark = 'LIKE' " +
            "GROUP BY r.eventId " +
            "ORDER BY COUNT(r) DESC")
    List<Long> findMostLikedEvents(Pageable pageable);
}
