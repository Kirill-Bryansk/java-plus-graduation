package ru.practicum.ewm.aggregator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Маппинг имен Kafka-топиков из конфига.
 * Позволяет менять имена топиков без перекомпиляции кода.
 */
@Data
@Component
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {
    /** Топик действий пользователей: stats.user-actions.v1 */
    private String statsUserActionV1;
    /** Топик сходства мероприятий: stats.events-similarity.v1 */
    private String statsEventsSimilarityV1;
}
