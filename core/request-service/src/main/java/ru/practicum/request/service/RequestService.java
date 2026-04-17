package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

/**
 * Сервис заявок на участие в событиях.
 * Предоставляет методы для создания, отмены и управления заявками,
 * а также批量 изменения статусов заявок инициатором события.
 */
public interface RequestService {

    /**
     * Создать заявку на участие в событии.
     * Проверяет: уникальность заявки, опубликованность события,
     * что пользователь не инициатор, и лимит участников.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return ParticipationRequestDto созданной заявки
     * @throws ConditionsNotMetException если нарушены условия создания заявки
     */
    ParticipationRequestDto createParticipationRequest(long userId, long eventId);

    /**
     * Получить все заявки пользователя на участие в событиях.
     *
     * @param userId идентификатор пользователя
     * @return список ParticipationRequestDto
     */
    List<ParticipationRequestDto> getAllByParticipantId(long userId);

    /**
     * Получить все заявки на участие в событии (только для инициатора).
     *
     * @param userId  идентификатор пользователя-инициатора
     * @param eventId идентификатор события
     * @return список ParticipationRequestDto
     * @throws ConditionsNotMetException если пользователь не инициатор события
     */
    List<ParticipationRequestDto> getAllByInitiatorIdAndEventId(long userId, long eventId);

    /**
     * Изменить статус заявок на участие в событии (подтвердить/отклонить).
     * Только для инициатора события. При подтверждении проверяет лимит участников.
     *
     * @param updateRequest данные для обновления (список ID заявок и новый статус)
     * @param userId        идентификатор пользователя-инициатора
     * @param eventId       идентификатор события
     * @return EventRequestStatusUpdateResult с результатами подтверждения и отклонения
     * @throws ConditionsNotMetException если пользователь не инициатор или заявки не в состоянии PENDING
     */
    EventRequestStatusUpdateResult changeEventRequestsStatusByInitiator(EventRequestStatusUpdateRequest updateRequest, long userId, long eventId);

    /**
     * Отменить заявку на участие в событии.
     *
     * @param userId    идентификатор пользователя
     * @param requestId идентификатор заявки
     * @return ParticipationRequestDto отменённой заявки
     * @throws NotFoundException если заявка не найдена
     * @throws ConditionsNotMetException если заявка не принадлежит пользователю
     */
    ParticipationRequestDto cancelParticipantRequest(long userId, long requestId);
}
