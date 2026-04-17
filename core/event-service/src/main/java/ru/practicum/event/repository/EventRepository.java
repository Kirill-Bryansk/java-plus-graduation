package ru.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий событий.
 * Поддерживает QueryDSL для динамических запросов с фильтрацией.
 * Предоставляет методы поиска событий по инициатору, ID и проверки существования.
 */
public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    /**
     * Найти событие по ID и ID инициатора (для приватного доступа).
     *
     * @param eventId   идентификатор события
     * @param initiatorId идентификатор пользователя-инициатора
     * @return Optional с найденным событием
     */
    Optional<Event> findByIdAndInitiatorId(long eventId, long initiatorId);

    /**
     * Найти все события пользователя-инициатора с пагинацией.
     *
     * @param initiatorId идентификатор пользователя
     * @param pageable    параметры пагинации
     * @return список событий
     */
    List<Event> findAllByInitiatorId(long initiatorId, Pageable pageable);

    /**
     * Проверить, есть ли события в данной категории.
     * Используется при удалении категории.
     *
     * @param categoryId идентификатор категории
     * @return true если категория содержит события
     */
    boolean existsByCategory_Id(long categoryId);

    /**
     * Найти события по списку ID.
     *
     * @param ids список идентификаторов событий
     * @return список найденных событий
     */
    List<Event> findAllByIdIn(List<Long> ids);

    /**
     * Проверить, является ли пользователь инициатором события.
     *
     * @param initiatorId идентификатор пользователя
     * @param eventId     идентификатор события
     * @return true если пользователь — создатель события
     */
    boolean existsByInitiatorIdAndId(long initiatorId, long eventId);
}
