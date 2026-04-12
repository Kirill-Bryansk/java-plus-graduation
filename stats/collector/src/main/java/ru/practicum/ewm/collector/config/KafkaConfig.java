package ru.practicum.ewm.collector.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.collector.serializer.GeneralAvroSerializer;

import java.util.Properties;

/**
 * Конфигурация Kafka-продюсера (напрямую, без Spring Kafka).
 * Использует Long (userId) как ключ для партиционирования —
 * действия одного пользователя всегда попадают в одну партицию.
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Нативный KafkaProducer для полного контроля:
     * - ключ: Long (userId)
     * - значение: SpecificRecordBase (Avro-запись)
     */
    @Bean
    public Producer<Long, SpecificRecordBase> kafkaProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        // Кастомный Avro-сериализатор (без Schema Registry)
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());
        return new KafkaProducer<>(config);
    }
}
