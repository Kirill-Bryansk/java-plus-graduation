package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.event.EventForRequestDto;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;

/**
 * Реализация внутреннего сервиса событий для межсервисного взаимодействия.
 * Используется rating-service для проверки существования событий.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EventInternalServiceImpl implements EventInternalService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    /**
     * Получить событие по ID для внутреннего использования.
     *
     * @param id идентификатор события
     * @return EventForRequestDto с данными события
     * @throws NotFoundException если событие не найдено
     */
    @Override
    public EventForRequestDto getById(Long id) {
        log.info("Получение события (внутреннее): id={}", id);
        Event event = eventRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id %d не найдено", id)));
        EventForRequestDto result = eventMapper.toEventForRequestDto(event);
        log.info("Событие найдено: id={}, state={}", result.getId(), result.getState());
        return result;
    }

    /**
     * Проверить, является ли пользователь инициатором события.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return true если пользователь — создатель события
     */
    @Override
    public boolean checkUserIsInitiator(long userId, long eventId) {
        log.info("Проверка инициатора: userId={}, eventId={}", userId, eventId);
        boolean result = eventRepository.existsByInitiatorIdAndId(userId, eventId);
        log.info("Результат проверки инициатора: {}", result);
        return result;
    }

    /**
     * Проверить существование события по ID.
     *
     * @param id идентификатор события
     * @throws NotFoundException если событие не найдено
     */
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
