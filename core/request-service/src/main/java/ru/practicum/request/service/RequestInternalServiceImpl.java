package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.request.repository.RequestRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RequestInternalServiceImpl implements RequestInternalService {

    private final RequestRepository requestRepository;

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

    @Override
    public int getCountConfirmedRequestsByEventId(Long eventId) {
        int count = requestRepository.findCountOfConfirmedRequestsByEventId(eventId);
        log.info("Количество подтверждённых заявок для eventId={}: {}", eventId, count);
        return count;
    }
}