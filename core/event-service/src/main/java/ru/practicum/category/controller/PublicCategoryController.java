package ru.practicum.category.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

/**
 * Публичный контроллер категорий.
 * Предоставляет endpoints для чтения категорий: получение списка и одной категории по ID.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
@Validated
public class PublicCategoryController {
    private final CategoryService categoryService;

    /**
     * Получить список категорий с пагинацией.
     *
     * @param from номер первого элемента (по умолчанию 0)
     * @param size количество элементов в выборке (по умолчанию 10)
     * @return список категорий в формате CategoryDto
     */
    @GetMapping
    public List<CategoryDto> getAllCategories(@RequestParam(name = "from", defaultValue = "0")
                                              @PositiveOrZero Integer from,
                                              @RequestParam(name = "size", defaultValue = "10")
                                              @Positive Integer size) {
        log.debug("GET: Запрос на получение категорий: from={}, size={}", from, size);
        return categoryService.getAllCategories(from, size);
    }

    /**
     * Получить категорию по ID.
     *
     * @param catId идентификатор категории
     * @return CategoryDto с информацией о категории
     */
    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable long catId) {
        log.debug("GET: Запрос на получение категории с id: {}", catId);
        return categoryService.getCategoryById(catId);
    }
}
