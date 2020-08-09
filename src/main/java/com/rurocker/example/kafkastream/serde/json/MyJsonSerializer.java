package com.rurocker.example.kafkastream.serde.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class MyJsonSerializer<T> implements Serializer<T> {

    private final ObjectMapper objectMapper;

    public MyJsonSerializer(final ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
        this.objectMapper.setTimeZone(TimeZone.getDefault());

        // for serialized LocalDateTime
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        this.objectMapper.registerModule(javaTimeModule);
    }

    public MyJsonSerializer() {
        this(new ObjectMapper());
    }

    @Override
    public byte[] serialize(final String topic, final T objectToSerialize) {
        if (objectToSerialize == null) {
            return new byte[0];
        }

        try {
            return objectMapper.writeValueAsBytes(objectToSerialize);
        } catch (final IOException e) {
            throw new SerializationException("Error serializing message", e);
        }
    }
}
