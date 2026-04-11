package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.request.repository.RequestRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация внутреннего сервиса заявок для межсервисного взаимодействия.
 * Использует RequestRepository для подсчёта подтверждённых заявок.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RequestInternalServiceImpl implements RequestInternalService {

    private final RequestRepository requestRepository;

    /**
     * Получить количество подтверждённых заявок для списка событий.
     *
     * @param eventsIds список идентификаторов событий
     * @return Map<event_id, confirmed_count>
     */
    @Override
    public Map<Long, Integer> getCountConfirmedRequestsByEventIds(List<Long> eventsIds) {
        Map<Long, Integer> result = requestRepository.findCountConfirmedByEventIds(eventsIds).stream()
                .collect(Collectors.toMap(
                        RequestRepository.EventRequestCount::getEventId,
                        RequestRepository.EventRequestCount::getCount
                ));
        log.info("Найдено количество подтверждённых заявок для {} событий", result.size());
        return result;
    }

    /**
     * Получить количество подтверждённых заявок для одного события.
     *
     * @param eventId идентификатор события
     * @return количество подтверждённых заявок
     */
    @Override
    public int getCountConfirmedRequestsByEventId(Long eventId) {
        int count = requestRepository.findCountOfConfirmedRequestsByEventId(eventId);
        log.info("Количество подтверждённых заявок для eventId={}: {}", eventId, count);
        return count;
    }
}