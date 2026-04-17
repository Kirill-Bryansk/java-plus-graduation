package ru.practicum.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

/**
 * Административный контроллер категорий.
 * Предоставляет endpoints для создания, удаления и обновления категорий.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    /**
     * Создать новую категорию.
     *
     * @param newCategoryDto данные новой категории (name)
     * @return созданная категория в формате CategoryDto
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.debug("POST: Запрос на создание категории: {}", newCategoryDto);
        return categoryService.addCategory(newCategoryDto);
    }

    /**
     * Удалить категорию по ID.
     * Проверяет, что категория не привязана к событиям.
     *
     * @param catId идентификатор категории
     */
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable long catId) {
        log.debug("DELETE: Запрос на удаление категории с id: {}", catId);
        categoryService.deleteCategory(catId);
    }

    /**
     * Обновить название категории.
     * Проверяет уникальность нового названия.
     *
     * @param catId        идентификатор категории
     * @param categoryDto  новое название категории
     * @return обновлённая категория в формате CategoryDto
     */
    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable long catId, @RequestBody @Valid NewCategoryDto categoryDto) {
        log.debug("PATCH: Запрос на обновление категории: catId={}, {}", catId, categoryDto);
        return categoryService.updateCategory(catId, categoryDto);
    }
}
