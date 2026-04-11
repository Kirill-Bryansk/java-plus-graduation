package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.errors.exceptions.DataAlreadyInUseException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.errors.exceptions.ValidationException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserAdminParam;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация сервиса пользователей.
 * Выполняет CRUD-операции, валидацию данных и пагинацию.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Получить список пользователей с пагинацией и фильтрацией по IDs.
     * Если IDs не указаны — возвращает всех пользователей.
     *
     * @param param параметры запроса (from, size, ids)
     * @return список UserDto
     */
    @Override
    public List<UserDto> getAllUsers(UserAdminParam param) {
        int from = param.getFrom();
        int size = param.getSize();
        log.info("Получение всех пользователей: from={}, size={}, ids={}", from, size, param.getIds());
        if (CollectionUtils.isEmpty(param.getIds())) {
            List<UserDto> result = userRepository.findAll(PageRequest.of(from, size)).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
            log.info("Получено {} пользователей (без фильтра по ids)", result.size());
            return result;
        }
        List<User> users = userRepository.findAllByIdIn(param.getIds(), PageRequest.of(from, size));
        log.info("Получено {} пользователей по ids={}", users.size(), param.getIds());
        return userMapper.toUserDtoList(users);
    }

    /**
     * Получить карту пользователей по списку ID.
     * Используется при межсервисных вызовах (Feign).
     *
     * @param userIds список идентификаторов
     * @return Map<ID, UserShortDto> найденных пользователей
     */
    @Override
    public Map<Long, UserShortDto> getAllUsersByIds(List<Long> userIds) {
        log.info("Получение пользователей по списку id: {}", userIds);
        List<User> users = userRepository.findAllByIdIn(userIds);
        Map<Long, UserShortDto> result = users.stream()
                .collect(Collectors.toMap(
                        User::getId,
                        userMapper::toUserShortDto
                ));
        log.info("Найдено {} пользователей из запрошенных {}", result.size(), userIds.size());
        return result;
    }

    /**
     * Получить краткую информацию о пользователе по ID.
     *
     * @param id идентификатор пользователя
     * @return UserShortDto
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public UserShortDto getById(Long id) {
        UserShortDto user = userMapper.toUserShortDto(findById(id));
        log.info("Пользователь найден: id={}, name={}", user.getId(), user.getName());
        return user;
    }

    /**
     * Проверить существование пользователя по ID.
     *
     * @param id идентификатор пользователя
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public void checkUserExists(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with id = " + id + " not found.");
        }
        log.info("Пользователь с id={} существует", id);
    }

    /**
     * Создать нового пользователя.
     * Проверяет уникальность email и корректность длины полей.
     *
     * @param newUserRequest данные нового пользователя
     * @return UserDto созданного пользователя
     * @throws ValidationException если длина полей некорректна
     * @throws DataAlreadyInUseException если email уже занят
     */
    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Создание пользователя: name={}, email={}", newUserRequest.getName(), newUserRequest.getEmail());
        // валидация длин (дублирует аннотации, но оставлено как есть)
        if (newUserRequest.getName().length() < 2 || newUserRequest.getName().length() > 250 ||
            newUserRequest.getEmail().length() < 6 || newUserRequest.getEmail().length() > 254) {
            throw new ValidationException("Length of name or email is out of bounds.");
        }
        if (userRepository.findByEmail(newUserRequest.getEmail()).isPresent()) {
            throw new DataAlreadyInUseException("Email " + newUserRequest.getEmail() + " already in use.");
        }
        User newUser = userMapper.toUser(newUserRequest);
        User created = userRepository.save(newUser);
        log.info("Пользователь создан с id={}", created.getId());
        return userMapper.toUserDto(created);
    }

    /**
     * Удалить пользователя по ID.
     *
     * @param userId идентификатор пользователя
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    @Transactional
    public void deleteUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id = " + userId + " not found.");
        }
        userRepository.deleteById(userId);
        log.info("Пользователь с id={} удалён", userId);
    }

    /**
     * Вспомогательный метод: найти пользователя или выбросить NotFoundException.
     *
     * @param userId идентификатор пользователя
     * @return найденный User
     * @throws NotFoundException если пользователь не найден
     */
    private User findById(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User with id = " + userId + " not found."));
    }
}