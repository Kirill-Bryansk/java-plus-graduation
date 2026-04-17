package ru.practicum.ewm.stats.client;

/**
 * Типы действий пользователя для отправки в Collector.
 * Соответствуют ActionTypeProto в gRPC-схеме.
 */
public enum ActionType {
    /** Просмотр страницы мероприятия */
    ACTION_VIEW,
    /** Заявка на участие в мероприятии */
    ACTION_REGISTER,
    /** Положительная оценка/лайк мероприятию */
    ACTION_LIKE
}
