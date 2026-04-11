package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ResponseStatDto;
import ru.practicum.dto.StatDto;
import ru.practicum.contract.StatsOperations;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsServerController implements StatsOperations {

    private final StatsService statsService;

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseStatDto> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                          @RequestParam(defaultValue = "", required = false) List<String> uris,
                                          @RequestParam(defaultValue = "false", required = false) boolean unique) {
        log.debug("GET: Запрос на получение статистики: start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        return statsService.getStat(start, end, uris, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public StatDto saveHitRequest(@RequestBody StatDto statDto) {
        log.debug("POST: Запрос на сохранение хита: app={}, uri={}, ip={}, timestamp={}",
                statDto.getApp(), statDto.getUri(), statDto.getIp(), statDto.getTimestamp());
        return statsService.saveRequest(statDto);
    }
}
