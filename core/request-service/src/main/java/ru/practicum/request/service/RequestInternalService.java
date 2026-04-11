package ru.practicum.request.service;

import java.util.List;
import java.util.Map;

/**
 * Внутренний сервис заявок для межсервисного взаимодействия.
 * Используется event-service для получения количества подтверждённых заявок
 * при отображении событий.
 */
public interface RequestInternalService {

    /**
     * Получить количество подтверждённых заявок для списка событий.
     *
     * @param eventsIds список идентификаторов событий
     * @return Map<event_id, confirmed_count>
     */
    Map<Long, Integer> getCountConfirmedRequestsByEventIds(List<Long> eventsIds);

    /**
     * Получить количество подтверждённых заявок для одного события.
     *
     * @param eventId идентификатор события
     * @return количество подтверждённых заявок
     */
    int getCountConfirmedRequestsByEventId(Long eventId);
}
