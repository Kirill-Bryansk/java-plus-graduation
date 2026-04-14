package ru.practicum.ewm.analyzer.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

import java.time.Duration;

/**
 * Интерфейс для доступа к Kafka Consumer'ам (Actions + Similarity).
 */
public interface KafkaClient {
    /** Consumer для чтения действий пользователей */
    Consumer<Long, SpecificRecordBase> getActionConsumer();
    /** Consumer для чтения сходства мероприятий */
    Consumer<Long, SpecificRecordBase> getSimilarityConsumer();
    Duration getPollTimeout();
    KafkaTopicsProperties getTopicsProperties();
}
