package ru.practicum.ewm.analyzer.dal.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.analyzer.dal.model.EventSimilarity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    /** Найти сходство по паре мероприятий */
    Optional<EventSimilarity> findByEventAAndEventB(long eventA, long eventB);

    /** Найти все сходства для конкретного мероприятия */
    @Query("select s from EventSimilarity s where s.eventA = :eventId or s.eventB = :eventId")
    List<EventSimilarity> findAllByEventId(@Param("eventId") long eventId);

    /**
     * Найти сходства мероприятий из списка с мероприятияМИ вне списка.
     * Используется для поиска "новых" рекомендаций, с которыми пользователь ещё не взаимодействовал.
     */
    @Query("select s from EventSimilarity s where (s.eventA IN :eventIds OR s.eventB IN :eventIds)")
    List<EventSimilarity> findByEventIdIn(@Param("eventIds") Set<Long> eventIds, Pageable pageable);
}
