package ru.practicum.ewm.collector.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.ewm.collector.serializer.GeneralAvroSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация Kafka-продюсера.
 * Настраивает сериализацию Avro-сообщений и подключение к Kafka-брокеру.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Свойства Kafka-продюсера.
     * - key.serializer: строки для ключей (userId:eventId)
     * - value.serializer: кастомный Avro-сериализатор для SpecificRecordBase
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Кастомный сериализатор: кодирует Avro-запись в бинарный формат
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);
        return props;
    }

    /**
     * KafkaTemplate для отправки Avro-сообщений.
     * Ключ — String, значение — Avro-запись (UserActionAvro, EventAvro).
     */
    @Bean
    public KafkaTemplate<String, SpecificRecordBase> kafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerConfigs()));
    }
}
