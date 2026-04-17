package ru.practicum.ewm.aggregator.serializer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * Кастомный десериализатор для Avro-сообщений.
 * Преобразует байтовый массив из Kafka обратно в Java-объект (SpecificRecordBase).
 */
public class UserActionAvroDeserializer implements Deserializer<SpecificRecordBase> {

    @Override
    public SpecificRecordBase deserialize(String topic, byte[] data) {
        try {
            // Определяем схему на основе класса UserActionAvro
            SpecificDatumReader<SpecificRecordBase> reader = new SpecificDatumReader<>(
                    UserActionAvro.getClassSchema()
            );
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка десериализации Avro", e);
        }
    }
}
