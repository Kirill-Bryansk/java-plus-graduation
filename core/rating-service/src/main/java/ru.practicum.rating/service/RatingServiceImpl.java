package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.errors.exceptions.ConditionsNotMetException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.rating.dto.NewRatingDto;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.UpdateRatingDto;
import ru.practicum.rating.mapper.RatingMapper;
import ru.practicum.rating.mark.Mark;
import ru.practicum.rating.model.Rating;
import ru.practicum.rating.repository.RatingRepository;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    @Transactional
    public RatingDto create(long userId, long eventId, NewRatingDto newRatingDto) {
        boolean exists = ratingRepository.existsByUserIdAndEventId(userId, eventId);
        if (exists) {
            log.warn("Пользователь {} уже оценивал событие {}", userId, eventId);
            throw new ConditionsNotMetException(
                    String.format("Пользователь с ID %d уже оценивал событие с ID %d", userId, eventId));
        }

        validateEventAndUser(eventId, userId);

        Rating rating = Rating.builder()
                .userId(userId)
                .eventId(eventId)
                .mark(newRatingDto.getMark())
                .build();

        Rating saved = ratingRepository.save(rating);
        log.info("Оценка создана: id={}, userId={}, eventId={}, mark={}",
                saved.getId(), saved.getUserId(), saved.getEventId(), saved.getMark());
        return ratingMapper.toRatingDto(saved);
    }

    @Override
    @Transactional
    public RatingDto update(long userId, long eventId, long ratingId, UpdateRatingDto updateRatingDto) {
        log.info("Начало обновления оценки: userId={}, eventId={}, ratingId={}, newMark={}",
                userId, eventId, ratingId, updateRatingDto.getMark());

        Rating rating = ratingRepository.findByIdAndUserId(ratingId, userId)
                .orElseThrow(() -> {
                    log.error("Оценка с ratingId={} и userId={} не найдена", ratingId, userId);
                    return new NotFoundException(String.format(
                            "Rating with userId = %d and id = %d was not found", userId, ratingId));
                });

        // Проверка, что оценка принадлежит указанному событию
        if (rating.getEventId() != eventId) {
            log.warn("Оценка {} принадлежит событию {}, а запрос пришёл для события {}",
                    ratingId, rating.getEventId(), eventId);
            throw new ConditionsNotMetException(
                    String.format("Rating %d does not belong to event %d", ratingId, eventId));
        }

        // Проверка существования события (на всякий случай, если событие уже удалено)
        validateEventAndUser(eventId, userId);

        Mark oldMark = rating.getMark();
        Mark newMark = updateRatingDto.getMark();

        if (!Objects.equals(oldMark, newMark)) {
            rating.setMark(newMark);
            log.info("Оценка обновлена: ratingId={}, старый Mark={}, новый Mark={}", ratingId, oldMark, newMark);
        } else {
            log.debug("Значение оценки не изменилось, обновление не требуется");
        }

        return ratingMapper.toRatingDto(rating);
    }

    @Override
    @Transactional
    public void delete(long userId, long eventId, long ratingId) {
        log.info("Начало удаления оценки: userId={}, eventId={}, ratingId={}", userId, eventId, ratingId);

        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> {
                    log.error("Оценка с id={} не найдена", ratingId);
                    return new NotFoundException(String.format("Rating mark not found. id = '%d'", ratingId));
                });

        if (userId != rating.getUserId()) {
            log.warn("Пользователь {} не является автором оценки {}", userId, ratingId);
            throw new ConditionsNotMetException("User with id = " + userId + " is not author of mark");
        }

        if (rating.getEventId() != eventId) {
            log.warn("Оценка {} принадлежит событию {}, а запрос на удаление пришёл для события {}",
                    ratingId, rating.getEventId(), eventId);
            throw new ConditionsNotMetException(
                    String.format("Rating %d does not belong to event %d", ratingId, eventId));
        }

        validateEventAndUser(eventId, userId); // проверка существования события (опционально, но для консистентности)

        ratingRepository.deleteById(ratingId);
        log.info("Оценка с id={} удалена", ratingId);
    }

    /**
     * Общая проверка существования события и пользователя через внешние клиенты.
     */
    private void validateEventAndUser(long eventId, long userId) {
        log.debug("Проверка существования события с id={}", eventId);
        eventClient.checkExistsById(eventId);
        log.debug("Проверка существования пользователя с id={}", userId);
        userClient.checkUserExists(userId);
    }
}