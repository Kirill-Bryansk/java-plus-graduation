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
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController implements UserOperations {

    private final UserService userService;

    /**
     * Получить карту пользователей по списку id.
     */
    @PostMapping
    public Map<Long, UserShortDto> getAllUsersByIds(@RequestBody List<Long> userIds) {
        log.debug("POST /internal/users: запрос пользователей по ids={}", userIds);
        Map<Long, UserShortDto> result = userService.getAllUsersByIds(userIds);
        log.debug("POST /internal/users: возвращено {} пользователей", result.size());
        return result;
    }

    /**
     * Получить одного пользователя по id.
     */
    @GetMapping("/{id}")
    public UserShortDto getById(@PathVariable Long id) {
        log.debug("GET /internal/users/{}: запрос пользователя", id);
        return userService.getById(id);
    }

    /**
     * Проверить существование пользователя.
     */
    @GetMapping("/check/{id}")
    public void checkUserExists(@PathVariable Long id) {
        log.debug("GET /internal/users/check/{}: проверка существования пользователя", id);
        userService.checkUserExists(id);
    }
}