package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.enums.request.RequestStatus;
import ru.practicum.request.model.Request;

import java.util.List;

/**
 * Репозиторий заявок на участие в событиях.
 * Предоставляет методы поиска заявок по пользователю и событию,
 * подсчёта подтверждённых заявок и批量 изменения статусов.
 */
public interface RequestRepository extends JpaRepository<Request, Long> {

    /**
     * Найти все заявки пользователя по ID участника.
     *
     * @param userId идентификатор пользователя
     * @return список заявок
     */
    List<Request> findAllByRequesterId(long userId);

    /**
     * Найти все заявки на участие в событии по ID события.
     *
     * @param eventId идентификатор события
     * @return список заявок
     */
    List<Request> findAllByEventId(long eventId);

    /**
     * Подсчитать количество подтверждённых заявок для события.
     *
     * @param eventId идентификатор события
     * @return количество подтверждённых заявок
     */
    @Query("select count(r) from Request r where r.eventId = :eventId and r.status = 'CONFIRMED'")
    int findCountOfConfirmedRequestsByEventId(long eventId);

    /**
     * Подсчитать количество подтверждённых заявок для списка событий.
     * Возвращает проекцию EventRequestCount для каждого события.
     *
     * @param eventsIds список идентификаторов событий
     * @return список проекций EventRequestCount
     */
    @Query("select r.eventId as eventId, count(r) as count " +
            "from Request r " +
            "where r.eventId in :eventsIds and r.status = 'CONFIRMED' " +
            "group by r.eventId")
    List<EventRequestCount> findCountConfirmedByEventIds(List<Long> eventsIds);

    /**
     * Обновить статус заявок批量.
     *
     * @param status новый статус
     * @param ids    список идентификаторов заявок
     */
    @Modifying
    @Transactional
    @Query("UPDATE Request r SET r.status = :status WHERE r.id IN :ids")
    void updateStatus(RequestStatus status, List<Long> ids);

    /**
     * Проверить, существует ли заявка от данного пользователя на данное событие.
     *
     * @param eventId    идентификатор события
     * @param requesterId идентификатор пользователя
     * @return true если заявка уже существует
     */
    boolean existsByEventIdAndRequesterId(long eventId, long requesterId);

    /**
     * Проекция для группировки количества заявок по событиям.
     */
    interface EventRequestCount {
        Long getEventId();

        Integer getCount();
    }

}
