package ru.practicum.ewm.analyzer.dal.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.analyzer.dal.model.UserAction;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    /** Найти действие пользователя с конкретным событием */
    Optional<UserAction> findByUserIdAndEventId(long userId, long eventId);

    /** Получить ID событий, с которыми взаимодействовал пользователь (кроме указанного) */
    @Query("select a.eventId from UserAction a where a.userId = :userId and a.eventId != :eventId")
    Set<Long> findEventIdsByUserIdExcludeEventId(@Param("userId") long userId, @Param("eventId") long eventId);

    /** Получить ID событий пользователя, отсортированные по времени (последние N) */
    @Query("select a.eventId from UserAction a where a.userId = :userId order by a.actionTimestamp desc")
    List<Long> findEventIdsByUserId(@Param("userId") long userId, Pageable pageable);

    /** Получить все действия для списка мероприятий */
    List<UserAction> findByEventIdIn(Set<Long> eventIds);

    /** Получить сумму весов для списка мероприятий */
    @Query("select a.eventId, sum(a.weight) from UserAction a where a.eventId in :eventIds group by a.eventId")
    List<Object[]> sumWeightsByEventId(@Param("eventIds") Set<Long> eventIds);
}
