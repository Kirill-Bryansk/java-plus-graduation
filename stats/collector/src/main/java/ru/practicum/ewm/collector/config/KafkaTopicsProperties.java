package ru.practicum.ewm.collector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Вынесенные имена Kafka-топиков в конфиг.
 * Позволяет менять имена топиков без пересборки кода.
 */
@Data
@Component
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {

    /** Топик действий пользователей: stats.user-actions.v1 */
    private String statsUserActionV1;
}
