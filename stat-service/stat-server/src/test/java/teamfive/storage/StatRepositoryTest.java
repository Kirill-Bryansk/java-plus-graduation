package teamfive.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import teamfive.model.Hit;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class StatRepositoryTest {

    @Autowired
    private StatRepository statRepository;

    private Hit hit1, hit2, hit3, hit4;

    @BeforeEach
    void setUp() {
        statRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();

        hit1 = new Hit();
        hit1.setApp("ewm-main-service");
        hit1.setUri("/events/1");
        hit1.setIp("192.168.1.1");
        hit1.setTimestamp(now.minusHours(2));

        hit2 = new Hit();
        hit2.setApp("ewm-main-service");
        hit2.setUri("/events/1");
        hit2.setIp("192.168.1.2");
        hit2.setTimestamp(now.minusHours(1));

        hit3 = new Hit();
        hit3.setApp("ewm-main-service");
        hit3.setUri("/events/2");
        hit3.setIp("192.168.1.1");
        hit3.setTimestamp(now.minusMinutes(30));

        hit4 = new Hit();
        hit4.setApp("ewm-main-service");
        hit4.setUri("/events/1");
        hit4.setIp("192.168.1.1");
        hit4.setTimestamp(now.minusMinutes(15));

        statRepository.saveAll(Arrays.asList(hit1, hit2, hit3, hit4));
    }

    @Test
    void getNonUniqueStat_ShouldReturnCorrectStats() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = Arrays.asList("/events/1", "/events/2");

        var result = statRepository.getNonUniqueStat(start, end, uris);

        assertThat(result).hasSize(2);

        var event1Stat = result.stream()
                .filter(stat -> stat.getUri().equals("/events/1"))
                .findFirst()
                .orElseThrow();
        assertThat(event1Stat.getHits()).isEqualTo(3L);

        var event2Stat = result.stream()
                .filter(stat -> stat.getUri().equals("/events/2"))
                .findFirst()
                .orElseThrow();
        assertThat(event2Stat.getHits()).isEqualTo(1L);
    }

    @Test
    void getUniqueStat_ShouldReturnUniqueIPCount() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = Arrays.asList("/events/1", "/events/2");

        var result = statRepository.getUniqueStat(start, end, uris);

        assertThat(result).hasSize(2);

        var event1Stat = result.stream()
                .filter(stat -> stat.getUri().equals("/events/1"))
                .findFirst()
                .orElseThrow();
        assertThat(event1Stat.getHits()).isEqualTo(2L);

        var event2Stat = result.stream()
                .filter(stat -> stat.getUri().equals("/events/2"))
                .findFirst()
                .orElseThrow();
        assertThat(event2Stat.getHits()).isEqualTo(1L);
    }

    @Test
    void getNonUniqueStat_WithEmptyUris_ShouldReturnEmptyList() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = Arrays.asList();

        var result = statRepository.getNonUniqueStat(start, end, uris);

        assertThat(result).isEmpty();
    }

    @Test
    void getUniqueStat_WithTimeRange_ShouldFilterCorrectly() {
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        List<String> uris = Arrays.asList("/events/1");

        var result = statRepository.getUniqueStat(start, end, uris);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHits()).isEqualTo(2L);
    }
}
