package teamfive.service;

import dto.InputHitDto;
import dto.OutHitDto;
import dto.StatDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import teamfive.mapper.SimpleHitMapper;
import teamfive.model.Hit;
import teamfive.model.StatHit;
import teamfive.storage.StatRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatServiceImplTest {

    @Mock
    private StatRepository statRepository;

    @Mock
    private SimpleHitMapper mapper;

    @InjectMocks
    private StatServiceImpl statService;

    @Test
    void getStats_WithUniqueTrue_ShouldCallUniqueStat() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/1");
        Boolean unique = true;

        List<StatHit> statHits = List.of(
                new StatHit("ewm-main-service", "/events/1", 10L)
        );

        List<StatDto> statDtos = List.of(
                createStatDto("ewm-main-service", "/events/1", 10L)
        );

        when(statRepository.getUniqueStat(start, end, uris)).thenReturn(statHits);
        when(mapper.statHitToStatDto(any(StatHit.class))).thenReturn(statDtos.get(0));

        List<StatDto> result = statService.getStats(start, end, uris, unique);

        verify(statRepository).getUniqueStat(start, end, uris);
        verify(statRepository, never()).getNonUniqueStat(any(), any(), any());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHits()).isEqualTo(10L);
    }

    @Test
    void getStats_WithUniqueFalse_ShouldCallNonUniqueStat() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/1");
        Boolean unique = false;

        List<StatHit> statHits = List.of(
                new StatHit("ewm-main-service", "/events/1", 15L)
        );

        List<StatDto> statDtos = List.of(
                createStatDto("ewm-main-service", "/events/1", 15L)
        );

        when(statRepository.getNonUniqueStat(start, end, uris)).thenReturn(statHits);
        when(mapper.statHitToStatDto(any(StatHit.class))).thenReturn(statDtos.get(0));

        List<StatDto> result = statService.getStats(start, end, uris, unique);

        verify(statRepository).getNonUniqueStat(start, end, uris);
        verify(statRepository, never()).getUniqueStat(any(), any(), any());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHits()).isEqualTo(15L);
    }

    @Test
    void getStats_WithEmptyUris_ShouldReturnEmptyList() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = Collections.emptyList();
        Boolean unique = true;

        List<StatDto> result = statService.getStats(start, end, uris, unique);

        assertThat(result).isEmpty();
    }

    @Test
    void getStats_WithNullUris_ShouldCallRepository() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = null;
        Boolean unique = true;

        List<StatHit> statHits = List.of(
                new StatHit("ewm-main-service", "/events/1", 5L)
        );

        when(statRepository.getUniqueStat(start, end, uris)).thenReturn(statHits);
        when(mapper.statHitToStatDto(any(StatHit.class))).thenReturn(createStatDto("ewm-main-service", "/events/1", 5L));

        List<StatDto> result = statService.getStats(start, end, uris, unique);

        verify(statRepository).getUniqueStat(start, end, uris);
        assertThat(result).hasSize(1);
    }

    @Test
    void getStats_ShouldMapAllResults() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/1", "/events/2");
        Boolean unique = false;

        List<StatHit> statHits = List.of(
                new StatHit("ewm-main-service", "/events/1", 10L),
                new StatHit("ewm-main-service", "/events/2", 5L)
        );

        List<StatDto> expectedDtos = List.of(
                createStatDto("ewm-main-service", "/events/1", 10L),
                createStatDto("ewm-main-service", "/events/2", 5L)
        );

        when(statRepository.getNonUniqueStat(start, end, uris)).thenReturn(statHits);
        when(mapper.statHitToStatDto(statHits.get(0))).thenReturn(expectedDtos.get(0));
        when(mapper.statHitToStatDto(statHits.get(1))).thenReturn(expectedDtos.get(1));

        List<StatDto> result = statService.getStats(start, end, uris, unique);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getHits()).isEqualTo(10L);
        assertThat(result.get(1).getHits()).isEqualTo(5L);
        verify(mapper, times(2)).statHitToStatDto(any(StatHit.class));
    }

    @Test
    void createHit_ShouldSaveAndReturnMappedDto() {
        InputHitDto inputDto = createInputHitDto();
        Hit savedHit = createHit();
        OutHitDto expectedDto = createOutHitDto();

        when(mapper.dtoToHit(inputDto)).thenReturn(savedHit);
        when(statRepository.save(savedHit)).thenReturn(savedHit);
        when(mapper.hitToDto(savedHit)).thenReturn(expectedDto);

        OutHitDto result = statService.createHit(inputDto);

        verify(mapper).dtoToHit(inputDto);
        verify(statRepository).save(savedHit);
        verify(mapper).hitToDto(savedHit);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getApp()).isEqualTo("ewm-main-service");
    }

    private InputHitDto createInputHitDto() {
        InputHitDto dto = new InputHitDto();
        dto.setApp("ewm-main-service");
        dto.setUri("/events/1");
        dto.setIp("192.168.1.1");
        dto.setTimestamp(LocalDateTime.now());
        return dto;
    }

    private Hit createHit() {
        Hit hit = new Hit();
        hit.setId(1L);
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.now());
        return hit;
    }

    private OutHitDto createOutHitDto() {
        OutHitDto dto = new OutHitDto();
        dto.setId(1);
        dto.setApp("ewm-main-service");
        dto.setUri("/events/1");
        dto.setIp("192.168.1.1");
        dto.setTimestamp(LocalDateTime.now());
        return dto;
    }

    private StatDto createStatDto(String app, String uri, Long hits) {
        StatDto dto = new StatDto();
        dto.setApp(app);
        dto.setUri(uri);
        dto.setHits(hits);
        return dto;
    }
}
