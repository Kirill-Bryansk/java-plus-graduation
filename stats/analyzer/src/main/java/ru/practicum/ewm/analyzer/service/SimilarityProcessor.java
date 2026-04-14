package ru.practicum.ewm.analyzer.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.analyzer.config.KafkaClient;
import ru.practicum.ewm.analyzer.handler.SimilarityHandler;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consumer-поток для обработки сходства мероприятий.
 * Читает из stats.events-similarity.v1 и сохраняет в БД.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SimilarityProcessor {

    private final KafkaClient kafkaClient;
    private final SimilarityHandler handler;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @PostConstruct
    public void init() {
        Thread thread = new Thread(this::start);
        thread.setName("kafka-similarity-thread");
        thread.setDaemon(false);
        thread.start();
        log.info("SimilarityProcessor запущен");
    }

    private void start() {
        var consumer = kafkaClient.getSimilarityConsumer();
        consumer.subscribe(List.of(kafkaClient.getTopicsProperties().getStatsEventsSimilarityV1()));
        log.info("SimilarityProcessor: подписка на {}", kafkaClient.getTopicsProperties().getStatsEventsSimilarityV1());

        try {
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<Long, SpecificRecordBase> records = consumer.poll(kafkaClient.getPollTimeout());
                for (ConsumerRecord<Long, SpecificRecordBase> record : records) {
                    if (record.value() instanceof EventSimilarityAvro similarity) {
                        handler.handle(similarity);
                    }
                    currentOffsets.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)
                    );
                }
                if (!records.isEmpty()) {
                    consumer.commitSync(currentOffsets);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка в SimilarityProcessor", e);
        } finally {
            consumer.close();
        }
    }
}
