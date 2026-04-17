package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationParam;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

/**
 * Сервис подборок событий.
 * Предоставляет методы для CRUD-операций с подборками и поиска с фильтрацией.
 */
public interface CompilationService {

    /**
     * Создать новую подборку событий.
     *
     * @param newCompilationDto данные новой подборки
     * @return CompilationDto созданной подборки
     */
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    /**
     * Удалить подборку по ID.
     *
     * @param compId идентификатор подборки
     * @throws NotFoundException если подборка не найдена
     */
    void deleteCompilation(long compId);

    /**
     * Обновить данные подборки (title, pinned, events).
     *
     * @param compId                   идентификатор подборки
     * @param updateCompilationRequest данные для обновления
     * @return CompilationDto обновлённой подборки
     * @throws NotFoundException если подборка не найдена
     */
    CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilationRequest);

    /**
     * Получить список подборок с пагинацией и фильтром по закреплённости.
     *
     * @param param параметры запроса (isPinned, from, size)
     * @return список CompilationDto
     */
    List<CompilationDto> getAllCompilations(CompilationParam param);

    /**
     * Получить подборку по ID.
     *
     * @param compId идентификатор подборки
     * @return CompilationDto
     * @throws NotFoundException если подборка не найдена
     */
    CompilationDto getCompilationById(long compId);
}
