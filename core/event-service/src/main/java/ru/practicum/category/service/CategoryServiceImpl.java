package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.errors.exceptions.ConditionsNotMetException;
import ru.practicum.errors.exceptions.DataAlreadyInUseException;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.repository.EventRepository;

import java.util.List;

/**
 * Реализация сервиса категорий.
 * Выполняет CRUD-операции, проверяет уникальность имён
 * и контролирует связь категорий с событиями.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    /**
     * Создать новую категорию.
     * Проверяет уникальность названия перед сохранением.
     *
     * @param newCategoryDto данные новой категории
     * @return CategoryDto созданной категории
     * @throws DataAlreadyInUseException если категория с таким именем уже существует
     */
    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Начало создания категории с именем = {}.", newCategoryDto.getName());
        checkCategoryOnExistByName(newCategoryDto.getName());
        Category newCategory = categoryMapper.toCategory(newCategoryDto);
        Category created = categoryRepository.save(newCategory);
        log.info("Категория {} с id = {} создана", created.getName(), created.getId());
        return categoryMapper.toCategoryDto(created);
    }

    /**
     * Удалить категорию по ID.
     * Проверяет, что категория не привязана к событиям.
     *
     * @param catId идентификатор категории
     * @throws NotFoundException если категория не найдена
     * @throws ConditionsNotMetException если категория содержит события
     */
    @Override
    @Transactional
    public void deleteCategory(long catId) {
        log.info("Начало удаления категории с id = {}", catId);
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with id = " + catId + " not found.");
        }
        if (eventRepository.existsByCategory_Id(catId)) {
            throw new ConditionsNotMetException("The category with id = " + catId + " is not empty");
        }

        categoryRepository.deleteById(catId);
        log.info("Категория с id {} удалена", catId);
    }

    /**
     * Обновить название категории.
     * Если название не изменилось — возвращает текущую категорию без изменений.
     *
     * @param catId        идентификатор категории
     * @param categoryDto  новые данные (name)
     * @return CategoryDto обновлённой категории
     * @throws NotFoundException если категория не найдена
     * @throws DataAlreadyInUseException если новое имя уже занято
     */
    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, NewCategoryDto categoryDto) {
        Category toUpdate = findByIdOrThrow(catId);
        String nameToUpdate = categoryDto.getName();
        log.info("Начало обновления категории с id = {}, имя для обновления = {}.", catId, categoryDto.getName());

        if (toUpdate.getName().equals(nameToUpdate)) {
            return categoryMapper.toCategoryDto(toUpdate);
        }
        checkCategoryOnExistByName(categoryDto.getName());
        toUpdate.setName(nameToUpdate);
        log.info("Категория с id = {} обновлена с новым именем {}", catId, nameToUpdate);
        return categoryMapper.toCategoryDto(toUpdate);
    }

    /**
     * Получить список категорий с пагинацией.
     *
     * @param from номер первого элемента
     * @param size количество элементов в выборке
     * @return список CategoryDto
     */
    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        log.info("Начало получения всех категорий с параметрами: from = {}, size = {}.", from, size);
        List<Category> categories = categoryRepository.findAll(PageRequest.of(from, size)).getContent();
        log.info("Получены все категории, количество = {}", categories.size());
        return categoryMapper.toCategoryDtoList(categories);
    }

    /**
     * Получить категорию по ID.
     *
     * @param catId идентификатор категории
     * @return CategoryDto
     * @throws NotFoundException если категория не найдена
     */
    @Override
    public CategoryDto getCategoryById(long catId) {
        log.info("Начало получения категории с id = {}", catId);
        Category finded = findByIdOrThrow(catId);
        log.info("Категория с id = {} найдена.", catId);
        return categoryMapper.toCategoryDto(finded);
    }

    /**
     * Найти категорию по ID или выбросить NotFoundException.
     *
     * @param catId идентификатор категории
     * @return найденная сущность Category
     * @throws NotFoundException если категория не найдена
     */
    @Override
    public Category findByIdOrThrow(long catId) {
        return categoryRepository.findById(catId).orElseThrow(() ->
                new NotFoundException("Category with id = " + catId + " not found."));
    }

    /**
     * Проверить существование категории по имени (регистронезависимо).
     *
     * @param name название категории
     * @throws DataAlreadyInUseException если категория уже существует
     */
    private void checkCategoryOnExistByName(String name) {
        if (categoryRepository.findByNameIgnoreCase(name.toLowerCase()).isPresent()) {
            throw new DataAlreadyInUseException("Category with this name has already exist.");
        }
    }
}