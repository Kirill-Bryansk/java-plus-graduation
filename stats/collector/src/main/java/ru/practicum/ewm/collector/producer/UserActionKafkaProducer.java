package ru.practicum.ewm.collector.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Обёртка над KafkaTemplate для отправки Avro-сообщений.
 * Логирует результат отправки и обрабатывает ошибки.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionKafkaProducer {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    /**
     * Отправить Avro-сообщение в Kafka-топик.
     *
     * @param topic   имя топика (stats.user-actions.v1)
     * @param key     ключ партиционирования (userId:eventId)
     * @param message Avro-запись (UserActionAvro, EventAvro)
     */
    public void send(String topic, String key, SpecificRecordBase message) {
        CompletableFuture<SendResult<String, SpecificRecordBase>> future = kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Сообщение отправлено: topic={}, partition={}, offset={}, key={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        key);
            } else {
                log.error("Ошибка отправки сообщения: topic={}, key={}", topic, key, ex);
            }
        });
    }
}
