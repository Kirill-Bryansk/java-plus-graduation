package teamfive.client;

import dto.InputHitDto;
import dto.StatDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StatClient {
    private final DiscoveryClient discoveryClient;
    private final String appName;
    private final RestClient restClient;

    public StatClient(RestClient restClient, DiscoveryClient discoveryClient,
                      @Value("${stat.app-name:ewm-service}") String appName) {
        this.restClient = restClient;
        this.discoveryClient = discoveryClient;
        this.appName = appName;
    }

    private URI getStatsServerUri() {
        List<ServiceInstance> instances = discoveryClient.getInstances("stat-server");
        if (instances.isEmpty()) {
            throw new RuntimeException("stat-server не найден в Eureka");
        }
        ServiceInstance instance = instances.get(0);
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort());
    }

    public void hit(HttpServletRequest request) {
        try {
            InputHitDto hitDto = new InputHitDto();
            hitDto.setApp(appName);
            hitDto.setUri(request.getRequestURI());
            hitDto.setIp(getClientIpAddress(request));
            hitDto.setTimestamp(LocalDateTime.now());

            restClient.post().uri(getStatsServerUri().resolve("/hit"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(hitDto)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Ошибка при отправке hit. {}", e.getMessage());
        }
    }

    public List<StatDto> getStats(ParamRequest paramRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss");
        String startFormatted =
                paramRequest.getStart().format(formatter);
        String endFormatted = paramRequest.getEnd().format(formatter);

        StringBuilder finalUrl = new StringBuilder(getStatsServerUri()
                                                   + "/stats?");
        finalUrl.append("start=").append(startFormatted);
        finalUrl.append("&end=").append(endFormatted);
        finalUrl.append("&unique=").append(paramRequest.getUnique());

        if (paramRequest.getUris() != null &&
            !paramRequest.getUris().isEmpty()) {
            for (String uri : paramRequest.getUris()) {
                finalUrl.append("&uris=").append(uri);
            }
        }

        return restClient.get()
                .uri(finalUrl.toString())
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, (req, res)
                        -> {
                    log.error("Ошибка статистики: {}",
                            res.getStatusCode());
                    throw new RuntimeException("Ошибка статистики: " +
                                               res.getStatusCode());
                })
                .body(new ParameterizedTypeReference<List<StatDto>>() {
                });
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}