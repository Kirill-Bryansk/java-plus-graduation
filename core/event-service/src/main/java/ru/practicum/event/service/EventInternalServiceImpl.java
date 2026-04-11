package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.event.EventForRequestDto;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class EventInternalServiceImpl implements EventInternalService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public EventForRequestDto getById(Long id) {
        log.info("Получение события (внутреннее): id={}", id);
        Event event = eventRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id %d не найдено", id)));
        EventForRequestDto result = eventMapper.toEventForRequestDto(event);
        log.info("Событие найдено: id={}, state={}", result.getId(), result.getState());
        return result;
    }

    @Override
    public boolean checkUserIsInitiator(long userId, long eventId) {
        log.info("Проверка инициатора: userId={}, eventId={}", userId, eventId);
        boolean result = eventRepository.existsByInitiatorIdAndId(userId, eventId);
        log.info("Результат проверки инициатора: {}", result);
        return result;
    }

    @Override
    public void checkExistsById(long id) {
        log.info("Проверка существования события: id={}", id);
        boolean exists = eventRepository.existsById(id);
        if (!exists) {
            throw new NotFoundException(String.format("Событие с id %d не найдено", id));
        }
        log.info("Событие с id={} существует", id);
    }
}
