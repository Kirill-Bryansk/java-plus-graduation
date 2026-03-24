package teamfive.integration;

import dto.InputHitDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import teamfive.storage.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class StatIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StatRepository statRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        statRepository.deleteAll();
    }

    @Test
    void fullIntegrationTest_CreateHitAndGetStats() {
        createTestHits();

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/1", "/events/2");

        var nonUniqueStats = statRepository.getNonUniqueStat(start, end, uris);
        var uniqueStats = statRepository.getUniqueStat(start, end, uris);

        assertThat(nonUniqueStats).hasSize(2);

        var event1NonUnique = findStatByUri(nonUniqueStats, "/events/1");
        assertThat(event1NonUnique.getHits()).isEqualTo(3L);

        var event2NonUnique = findStatByUri(nonUniqueStats, "/events/2");
        assertThat(event2NonUnique.getHits()).isEqualTo(1L);

        assertThat(uniqueStats).hasSize(2);

        var event1Unique = findStatByUri(uniqueStats, "/events/1");
        assertThat(event1Unique.getHits()).isEqualTo(2L);

        var event2Unique = findStatByUri(uniqueStats, "/events/2");
        assertThat(event2Unique.getHits()).isEqualTo(1L);
    }

    @Test
    void integrationTest_EmptyUris_ShouldReturnEmptyList() {
        createTestHits();

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of();

        var result = statRepository.getNonUniqueStat(start, end, uris);

        assertThat(result).isEmpty();
    }

    @Test
    void integrationTest_TimeRangeFiltering() {
        createTestHits();

        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        List<String> uris = List.of("/events/1");

        var result = statRepository.getUniqueStat(start, end, uris);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHits()).isEqualTo(2L);
    }

    @Test
    void integrationTest_NonExistingUris_ShouldReturnEmptyList() {
        createTestHits();

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/non-existing");

        var result = statRepository.getNonUniqueStat(start, end, uris);

        assertThat(result).isEmpty();
    }

    @Test
    void integrationTest_OrderByHitsDesc() {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 5; i++) {
            createHit("ewm-main-service", "/events/popular", "192.168.1." + i, now.minusHours(i));
        }

        for (int i = 0; i < 3; i++) {
            createHit("ewm-main-service", "/events/medium", "192.168.2." + i, now.minusHours(i));
        }

        createHit("ewm-main-service", "/events/rare", "192.168.3.1", now.minusHours(1));

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/events/popular", "/events/medium", "/events/rare");

        var result = statRepository.getNonUniqueStat(start, end, uris);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getUri()).isEqualTo("/events/popular");
        assertThat(result.get(0).getHits()).isEqualTo(5L);
        assertThat(result.get(1).getUri()).isEqualTo("/events/medium");
        assertThat(result.get(1).getHits()).isEqualTo(3L);
        assertThat(result.get(2).getUri()).isEqualTo("/events/rare");
        assertThat(result.get(2).getHits()).isEqualTo(1L);
    }

    private void createTestHits() {
        LocalDateTime now = LocalDateTime.now();

        createHit("ewm-main-service", "/events/1", "192.168.1.1", now.minusHours(2));
        createHit("ewm-main-service", "/events/1", "192.168.1.2", now.minusHours(1));
        createHit("ewm-main-service", "/events/1", "192.168.1.1", now.minusMinutes(30));

        createHit("ewm-main-service", "/events/2", "192.168.1.1", now.minusMinutes(15));
    }

    private void createHit(String app, String uri, String ip, LocalDateTime timestamp) {
        InputHitDto hitDto = new InputHitDto();
        hitDto.setApp(app);
        hitDto.setUri(uri);
        hitDto.setIp(ip);
        hitDto.setTimestamp(timestamp);

        var hit = statRepository.save(createHitFromDto(hitDto));
        assertThat(hit.getId()).isNotNull();
    }

    private teamfive.model.Hit createHitFromDto(InputHitDto dto) {
        teamfive.model.Hit hit = new teamfive.model.Hit();
        hit.setApp(dto.getApp());
        hit.setUri(dto.getUri());
        hit.setIp(dto.getIp());
        hit.setTimestamp(dto.getTimestamp());
        return hit;
    }

    private teamfive.model.StatHit findStatByUri(List<teamfive.model.StatHit> stats, String uri) {
        return stats.stream()
                .filter(stat -> stat.getUri().equals(uri))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Stat for URI " + uri + " not found"));
    }
}
