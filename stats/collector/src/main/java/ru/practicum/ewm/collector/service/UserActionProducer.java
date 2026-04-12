package ru.practicum.ewm.collector.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.collector.config.KafkaTopicsProperties;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;

/**
 * Обёртка над KafkaProducer для отправки Avro-сообщений.
 * Закрывает продюсер при остановке приложения (flush + graceful shutdown).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionProducer {

    private final Producer<Long, SpecificRecordBase> producer;
    private final KafkaTopicsProperties kafkaTopics;

    /**
     * Отправить Avro-запись в Kafka с ключом (userId) и timestamp.
     */
    private void send(String topic, SpecificRecordBase event, long timestamp, Long key) {
        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                topic, null, timestamp, key, event);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка отправки: topic={}, key={}", topic, key, exception);
            } else {
                log.debug("Отправлено: topic={}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    /**
     * Принять UserActionAvro, извлечь userId как ключ и отправить в Kafka.
     */
    public void sendUserAction(SpecificRecordBase userAction) {
        UserActionAvro avroAction = (UserActionAvro) userAction;
        Long userId = avroAction.getUserId();
        long timestamp = avroAction.getTimestamp().toEpochMilli();
        send(kafkaTopics.getStatsUserActionV1(), avroAction, timestamp, userId);
    }

    /**
     * Graceful shutdown: flush буфера и закрыть соединение.
     */
    @PreDestroy
    public void stop() {
        producer.flush();
        producer.close(Duration.ofSeconds(30));
    }
}
