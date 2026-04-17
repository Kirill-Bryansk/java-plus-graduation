package ru.practicum.compilation.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

/**
 * Репозиторий подборок событий.
 * Предоставляет методы поиска подборок с фильтрацией по закреплённости
 * и загрузкой связанных событий.
 */
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    /**
     * Найти все подборки с указанным значением pinned, с пагинацией.
     * Использует FETCH JOIN для загрузки связанных событий.
     *
     * @param pinned      значение закреплённости (true/false)
     * @param pageRequest параметры пагинации
     * @return список подборки с загруженными событиями
     */
    @Query("SELECT DISTINCT c FROM Compilation c " +
            "LEFT JOIN FETCH c.events e " +
            "WHERE c.pinned = :pinned")
    List<Compilation> findAllByPinned(Boolean pinned, PageRequest pageRequest);
}
