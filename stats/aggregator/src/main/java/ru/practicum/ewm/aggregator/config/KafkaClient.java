package ru.practicum.ewm.aggregator.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

import java.time.Duration;

/**
 * Интерфейс для доступа к Kafka Producer и Consumer.
 * Позволяет абстрагироваться от конкретной реализации клиента.
 */
public interface KafkaClient {
    Consumer<Long, SpecificRecordBase> getConsumer();
    Producer<Long, SpecificRecordBase> getProducer();
    Duration getPollTimeout();
    KafkaTopicsProperties getTopicsProperties();
}
