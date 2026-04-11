package ru.practicum.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий пользователей.
 * Предоставляет методы поиска по ID, email и массовой загрузки.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Найти пользователей по списку ID с пагинацией.
     *
     * @param ids      список идентификаторов
     * @param pageable параметры пагинации
     * @return список найденных пользователей
     */
    List<User> findAllByIdIn(List<Long> ids, Pageable pageable);

    /**
     * Найти пользователей по списку ID (без пагинации).
     *
     * @param ids список идентификаторов
     * @return список найденных пользователей
     */
    List<User> findAllByIdIn(List<Long> ids);

    /**
     * Найти пользователя по email.
     *
     * @param email адрес электронной почты
     * @return Optional с найденным пользователем
     */
    Optional<User> findByEmail(String email);

}
