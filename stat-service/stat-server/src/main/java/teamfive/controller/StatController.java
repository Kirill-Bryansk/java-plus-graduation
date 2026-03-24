package teamfive.controller;

import dto.InputHitDto;
import dto.OutHitDto;
import dto.StatDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import teamfive.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @PostMapping("/hit")
    public ResponseEntity<OutHitDto> createHit(@RequestBody @Valid InputHitDto inputHitDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(statService.createHit(inputHitDto));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<StatDto>> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                                  @RequestParam(required = false) List<String> uris,
                                                  @RequestParam(defaultValue = "false") Boolean unique) {
        if (end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата окончания не может быть раньше даты начала!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(statService.getStats(start, end, uris, unique));
    }

}
