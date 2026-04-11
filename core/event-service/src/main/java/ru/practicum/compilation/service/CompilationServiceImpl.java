package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationParam;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.errors.exceptions.NotFoundException;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация сервиса подборок событий.
 * Выполняет CRUD-операции, загрузку связанных событий через EventService
 * и пагинацию с фильтрацией по закреплённости.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventService eventService;

    /**
     * Создать новую подборку событий.
     * Загружает события по ID из event-service и связывает с подборкой.
     *
     * @param newCompilationDto данные новой подборки
     * @return CompilationDto созданной подборки
     */
    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание подборки: title={}, events={}", newCompilationDto.getTitle(), newCompilationDto.getEvents());
        List<Event> eventList = eventService.getAllByIds(newCompilationDto.getEvents());
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto);
        compilation.setEvents(eventList);
        compilationRepository.save(compilation);
        CompilationDto result = mapToDto(compilation);
        log.info("Подборка создана с id={}", result.getId());
        return result;
    }

    /**
     * Удалить подборку по ID.
     *
     * @param compId идентификатор подборки
     * @throws NotFoundException если подборка не найдена
     */
    @Override
    @Transactional
    public void deleteCompilation(long compId) {
        log.info("Удаление подборки: compId={}", compId);
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id = " + compId + " not found.");
        }
        compilationRepository.deleteById(compId);
        log.info("Подборка с id={} удалена", compId);
    }

    /**
     * Обновить данные подборки: title, pinned и список событий.
     * Обновляются только переданные поля (null-поля игнорируются).
     *
     * @param compId                   идентификатор подборки
     * @param updateCompilationRequest данные для обновления
     * @return CompilationDto обновлённой подборки
     * @throws NotFoundException если подборка не найдена
     */
    @Override
    @Transactional
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Обновление подборки: compId={}, pinned={}, title={}",
                compId, updateCompilationRequest.getPinned(), updateCompilationRequest.getTitle());
        Compilation compilationFromTable = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Compilation with id = " + compId + " not found."));

        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            compilationFromTable.setEvents(eventService.getAllByIds(updateCompilationRequest.getEvents()));
        }
        if (updateCompilationRequest.getPinned() != null)
            compilationFromTable.setPinned(updateCompilationRequest.getPinned());
        if (updateCompilationRequest.getTitle() != null)
            compilationFromTable.setTitle(updateCompilationRequest.getTitle());
        compilationFromTable = compilationRepository.save(compilationFromTable);
        CompilationDto result = mapToDto(compilationFromTable);
        log.info("Подборка обновлена: id={}", result.getId());
        return result;
    }

    /**
     * Получить список подборок с пагинацией и фильтром по закреплённости.
     *
     * @param param параметры запроса (isPinned, from, size)
     * @return список CompilationDto
     */
    public List<CompilationDto> getAllCompilations(CompilationParam param) {
        log.info("Получение подборок: isPinned={}, from={}, size={}", param.getIsPinned(), param.getFrom(), param.getSize());
        Boolean isPinned = param.getIsPinned();
        int from = param.getFrom();
        int size = param.getSize();

        List<Compilation> compilations = compilationRepository.findAllByPinned(isPinned, PageRequest.of(from / size, size));
        List<CompilationDto> result = mapToDtoList(compilations);
        log.info("Получено {} подборок", result.size());
        return result;
    }

    /**
     * Получить подборку по ID с загруженными событиями.
     *
     * @param compId идентификатор подборки
     * @return CompilationDto
     * @throws NotFoundException если подборка не найдена
     */
    @Override
    public CompilationDto getCompilationById(long compId) {
        log.info("Получение подборки: compId={}", compId);
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Compilation with id = " + compId + " not found."));
        CompilationDto result = mapToDto(compilation);
        log.info("Подборка найдена: id={}, title={}", result.getId(), result.getTitle());
        return result;
    }

    /**
     * Преобразовать список сущностей Compilation в список CompilationDto
     * с заполненными EventShortDto для каждого события.
     *
     * @param compilations список сущностей Compilation
     * @return список CompilationDto
     */
    private List<CompilationDto> mapToDtoList(List<Compilation> compilations) {

        Set<Event> events = compilations.stream()
                .flatMap(c -> c.getEvents().stream())
                .collect(Collectors.toSet());

        List<EventShortDto> eventsDto = eventService.getShortEvents(events.stream().toList());

        Map<Long, EventShortDto> eventDtoById = eventsDto.stream()
                .collect(Collectors.toMap(EventShortDto::getId, dto -> dto));


        return compilations.stream()
                .map(compilation -> {
                    List<EventShortDto> compilationEvents = compilation.getEvents().stream()
                            .map(Event::getId)
                            .map(eventDtoById::get)
                            .toList();

                    CompilationDto dto = compilationMapper.toCompilationDto(compilation);
                    dto.setEvents(compilationEvents);
                    return dto;
                })
                .toList();
    }

    /**
     * Преобразовать одну сущность Compilation в CompilationDto
     * с заполненными EventShortDto.
     *
     * @param compilation сущность Compilation
     * @return CompilationDto
     */
    private CompilationDto mapToDto(Compilation compilation) {
        List<EventShortDto> eventsToSet = eventService.getShortEvents(compilation.getEvents());
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);
        compilationDto.setEvents(eventsToSet);
        return compilationDto;
    }
}
