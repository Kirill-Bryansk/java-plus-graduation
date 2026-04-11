package ru.practicum.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.model.Category;

import java.util.Optional;

/**
 * Репозиторий категорий.
 * Предоставляет методы поиска категорий по имени и стандартные CRUD-операции.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Найти категорию по имени (регистронезависимо).
     *
     * @param name название категории
     * @return Optional с найденной категорией
     */
    Optional<Category> findByNameIgnoreCase(String name);
}
