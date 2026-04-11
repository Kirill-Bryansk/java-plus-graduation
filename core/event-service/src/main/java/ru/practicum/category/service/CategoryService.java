package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.errors.exceptions.NotFoundException;

import java.util.List;

/**
 * Сервис категорий.
 * Предоставляет методы для CRUD-операций с категориями,
 * поиска с пагинацией и проверки существования.
 */
public interface CategoryService {

    /**
     * Создать новую категорию.
     * Проверяет уникальность названия.
     *
     * @param newCategoryDto данные новой категории
     * @return CategoryDto созданной категории
     * @throws DataAlreadyInUseException если категория с таким именем уже существует
     */
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    /**
     * Удалить категорию по ID.
     *
     * @param catId идентификатор категории
     * @throws NotFoundException если категория не найдена
     * @throws ConditionsNotMetException если категория привязана к событиям
     */
    void deleteCategory(long catId);

    /**
     * Обновить название категории.
     * Проверяет уникальность нового названия.
     *
     * @param catId        идентификатор категории
     * @param categoryDto  новые данные (name)
     * @return CategoryDto обновлённой категории
     * @throws NotFoundException если категория не найдена
     * @throws DataAlreadyInUseException если новое имя уже занято
     */
    CategoryDto updateCategory(long catId, NewCategoryDto categoryDto);

    /**
     * Получить список категорий с пагинацией.
     *
     * @param from номер первого элемента
     * @param size количество элементов в выборке
     * @return список CategoryDto
     */
    List<CategoryDto> getAllCategories(Integer from, Integer size);

    /**
     * Получить категорию по ID.
     *
     * @param catId идентификатор категории
     * @return CategoryDto
     * @throws NotFoundException если категория не найдена
     */
    CategoryDto getCategoryById(long catId);

    /**
     * Найти категорию по ID или выбросить NotFoundException.
     *
     * @param catId идентификатор категории
     * @return найденная сущность Category
     * @throws NotFoundException если категория не найдена
     */
    Category findByIdOrThrow(long catId) throws NotFoundException;
}
