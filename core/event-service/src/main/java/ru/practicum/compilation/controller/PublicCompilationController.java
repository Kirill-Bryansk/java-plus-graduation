package ru.practicum.compilation.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationParam;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

/**
 * Публичный контроллер подборок событий.
 * Предоставляет endpoints для чтения подборок: получение списка и одной подборки по ID.
 */
@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
@Slf4j
public class PublicCompilationController {
    private final CompilationService compilationService;

    /**
     * Получить список подборок с пагинацией и фильтром по закреплённости.
     *
     * @param pinned фильтр по закреплённости (true/false, по умолчанию false)
     * @param from   номер первого элемента (по умолчанию 0)
     * @param size   количество элементов в выборке (по умолчанию 10)
     * @return ResponseEntity со списком CompilationDto
     */
    @GetMapping
    public ResponseEntity<List<CompilationDto>> getAllCompilations(
            @RequestParam(defaultValue = "false") String pinned,
            @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size) {
        log.debug("GET: Запрос на получение подборок: pinned={}, from={}, size={}", pinned, from, size);
        CompilationParam param = new CompilationParam();
        param.setIsPinned(Boolean.valueOf(pinned));
        param.setFrom(from);
        param.setSize(size);
        return ResponseEntity.status(HttpStatus.OK).body(compilationService.getAllCompilations(param));
    }

    /**
     * Получить подборку по ID.
     *
     * @param compId идентификатор подборки
     * @return ResponseEntity с CompilationDto
     */
    @GetMapping("/{compId}")
    public ResponseEntity<CompilationDto> getCompilationById(@PathVariable long compId) {
        log.debug("GET: Запрос на получение подборки с id: {}", compId);
        return ResponseEntity.status(HttpStatus.OK).body(compilationService.getCompilationById(compId));
    }
}
