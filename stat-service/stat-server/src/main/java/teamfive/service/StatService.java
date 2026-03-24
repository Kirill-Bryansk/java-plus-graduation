package teamfive.service;

import dto.InputHitDto;
import dto.OutHitDto;
import dto.StatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    List<StatDto> getStats(LocalDateTime start,
                           LocalDateTime end,
                           List<String> uris,
                           Boolean unique);

    OutHitDto createHit(InputHitDto inputHitDto);
}
