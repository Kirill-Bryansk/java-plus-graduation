package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

/**
 * Административный контроллер подборок событий.
 * Предоставляет endpoints для создания, удаления и обновления подборок.
 */
@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationController {
    private final CompilationService compilationService;

    /**
     * Создать новую подборку событий.
     *
     * @param newCompilationDto данные новой подборки (title, pinned, events)
     * @return ResponseEntity с созданной подборкой в формате CompilationDto
     */
    @PostMapping
    public ResponseEntity<CompilationDto> createCompilation(@Valid @RequestBody
                                                            NewCompilationDto newCompilationDto) {
        log.debug("POST: Запрос на создание подборки: {}", newCompilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(compilationService.createCompilation(newCompilationDto));
    }

    /**
     * Удалить подборку по ID.
     *
     * @param compId идентификатор подборки
     */
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable long compId) {
        log.debug("DELETE: Запрос на удаление подборки с id: {}", compId);
        compilationService.deleteCompilation(compId);
    }

    /**
     * Обновить данные подборки.
     * Можно обновить title, pinned и список событий.
     *
     * @param compId                   идентификатор подборки
     * @param updateCompilationRequest данные для обновления
     * @return ResponseEntity с обновлённой подборкой в формате CompilationDto
     */
    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(
            @PathVariable long compId,
            @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.debug("PATCH: Запрос на обновление подборки: compId={}, {}", compId, updateCompilationRequest);
        return ResponseEntity.status(HttpStatus.OK)
                .body(compilationService.updateCompilation(compId, updateCompilationRequest));
    }
}
