package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.contract.UserOperations;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.service.UserService;

import java.util.List;
import java.util.Map;

/**
 * Внутренний контроллер для вызовов между сервисами (Feign).
 * Предоставляет endpoints для получения пользователей другими микросервисами
 * (event-service, rating-service). Не предназначен для внешних вызовов.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController implements UserOperations {

    private final UserService userService;

    /**
     * Получить карту пользователей по списку ID.
     * Используется Feign-клиентами для массовой загрузки пользователей.
     *
     * @param userIds список идентификаторов пользователей
     * @return Map<ID, UserShortDto> найденных пользователей
     */
    @PostMapping
    public Map<Long, UserShortDto> getAllUsersByIds(@RequestBody List<Long> userIds) {
        log.debug("POST /internal/users: запрос пользователей по ids={}", userIds);
        Map<Long, UserShortDto> result = userService.getAllUsersByIds(userIds);
        log.debug("POST /internal/users: возвращено {} пользователей", result.size());
        return result;
    }

    /**
     * Получить одного пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @return UserShortDto с краткой информацией о пользователе
     */
    @GetMapping("/{id}")
    public UserShortDto getById(@PathVariable Long id) {
        log.debug("GET /internal/users/{}: запрос пользователя", id);
        return userService.getById(id);
    }

    /**
     * Проверить существование пользователя.
     * Возвращает 200 если пользователь найден, 404 если нет.
     * Используется другими сервисами для валидации перед операциями.
     *
     * @param id идентификатор пользователя
     */
    @GetMapping("/check/{id}")
    public void checkUserExists(@PathVariable Long id) {
        log.debug("GET /internal/users/check/{}: проверка существования пользователя", id);
        userService.checkUserExists(id);
    }
}