package ru.practicum.ewm.collector.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Кастомный Kafka-сериализатор для Avro-записей.
 * Кодирует SpecificRecordBase в бинарный массив через BinaryEncoder.
 */
public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }
        try {
            // DatumWriter записывает Avro-запись в поток
            SpecificDatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // BinaryEncoder — компактный бинарный формат (меньше JSON)
            BinaryEncoder encoder = EncoderFactory.get().directBinaryEncoder(out, null);
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Ошибка сериализации Avro-сообщения", e);
        }
    }
}
