package ru.practicum.user.service;

import ru.practicum.dto.user.UserShortDto;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserAdminParam;
import ru.practicum.user.dto.UserDto;

import java.util.List;
import java.util.Map;

/**
 * Сервис пользователей.
 * Предоставляет методы для CRUD-операций с пользователями,
 * поиска с пагинацией и проверки существования.
 */
public interface UserService {

    /**
     * Получить список пользователей с пагинацией и фильтрацией по IDs.
     *
     * @param params параметры запроса (from, size, ids)
     * @return список UserDto
     */
    List<UserDto> getAllUsers(UserAdminParam params);

    /**
     * Получить карту пользователей по списку ID.
     * Используется при межсервисных вызовах (Feign).
     *
     * @param userIds список идентификаторов
     * @return Map<ID, UserShortDto> найденных пользователей
     */
    Map<Long, UserShortDto> getAllUsersByIds(List<Long> userIds);

    /**
     * Получить краткую информацию о пользователе по ID.
     *
     * @param id идентификатор пользователя
     * @return UserShortDto
     * @throws NotFoundException если пользователь не найден
     */
    UserShortDto getById(Long id);

    /**
     * Проверить существование пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @throws NotFoundException если пользователь не найден
     */
    void checkUserExists(Long id);

    /**
     * Создать нового пользователя.
     * Проверяет уникальность email.
     *
     * @param newUserRequest данные нового пользователя
     * @return UserDto созданного пользователя
     * @throws DataAlreadyInUseException если email уже занят
     */
    UserDto createUser(NewUserRequest newUserRequest);

    /**
     * Удалить пользователя по ID.
     *
     * @param userId идентификатор пользователя
     * @throws NotFoundException если пользователь не найден
     */
    void deleteUser(long userId);
}
