package ru.practicum.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserAdminParam;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

/**
 * Административный контроллер пользователей.
 * Предоставляет endpoints для создания, удаления и поиска пользователей
 * с поддержкой пагинации и фильтрации по ID.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;

    /**
     * Получить список пользователей с пагинацией и фильтрацией.
     *
     * @param ids  список идентификаторов для фильтрации (необязательный)
     * @param from номер первого элемента (по умолчанию 0)
     * @param size количество элементов в выборке (по умолчанию 10)
     * @return список пользователей в формате UserDto
     */
    @GetMapping
    @Validated
    public List<UserDto> getAllUsers(@RequestParam(required = false) List<Long> ids,
                                     @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                     @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.debug("GET: Запрос на получение пользователей: ids={}, from={}, size={}", ids, from, size);
        UserAdminParam params = new UserAdminParam();
        params.setFrom(from);
        params.setSize(size);
        params.setIds(ids);
        return userService.getAllUsers(params);
    }

    /**
     * Создать нового пользователя.
     * Проверяет корректность входных данных и уникальность email.
     *
     * @param newUserRequest данные нового пользователя (name, email)
     * @return созданный пользователь в формате UserDto
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.debug("POST: Запрос на создание пользователя: {}", newUserRequest);
        return userService.createUser(newUserRequest);
    }

    /**
     * Удалить пользователя по ID.
     *
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long userId) {
        log.debug("DELETE: Запрос на удаление пользователя с id: {}", userId);
        userService.deleteUser(userId);
    }
}