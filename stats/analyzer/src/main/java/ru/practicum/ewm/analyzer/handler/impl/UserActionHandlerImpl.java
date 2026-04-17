package ru.practicum.ewm.analyzer.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.dal.model.UserAction;
import ru.practicum.ewm.analyzer.dal.repository.UserActionRepository;
import ru.practicum.ewm.analyzer.handler.UserActionHandler;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Сохраняет действия пользователей в БД.
 * Хранит только максимальный вес для пары (userId, eventId).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionHandlerImpl implements UserActionHandler {

    // Веса действий (должны совпадать с Aggregator)
    private static final Map<ActionTypeAvro, Double> WEIGHTS = Map.of(
            ActionTypeAvro.ACTION_VIEW, 0.4,
            ActionTypeAvro.ACTION_REGISTER, 0.8,
            ActionTypeAvro.ACTION_LIKE, 1.0
    );

    private final UserActionRepository repository;

    @Override
    public void handle(UserActionAvro action) {
        double weight = WEIGHTS.getOrDefault(action.getActionType(), 0.0);

        // Проверяем, есть ли уже действие с большим весом
        Optional<UserAction> existing = repository.findByUserIdAndEventId(action.getUserId(), action.getEventId());
        if (existing.isPresent() && existing.get().getWeight() >= weight) {
            // Старый вес больше или равен — не обновляем
            return;
        }

        UserAction userAction = existing.orElseGet(UserAction::new);
        userAction.setUserId(action.getUserId());
        userAction.setEventId(action.getEventId());
        userAction.setWeight(weight);
        userAction.setActionTimestamp(action.getTimestamp());

        repository.save(userAction);
        log.debug("Сохранено действие: userId={}, eventId={}, weight={}", action.getUserId(), action.getEventId(), weight);
    }
}
