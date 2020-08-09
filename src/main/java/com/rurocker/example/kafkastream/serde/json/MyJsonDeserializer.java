package com.rurocker.example.kafkastream.serde.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class MyJsonDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> forType;

    public MyJsonDeserializer(final ObjectMapper objectMapper, final Class<T> forType) {
        this.objectMapper = objectMapper;
        this.forType = forType;
        this.objectMapper.setTimeZone(TimeZone.getDefault());

        // for deserialized LocalDateTime
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        this.objectMapper.registerModule(javaTimeModule);

    }

    public MyJsonDeserializer(final Class<T> forType) {
        this(new ObjectMapper(), forType);
    }

    @Override
    public T deserialize(final String topic, final byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        try {
            return objectMapper.readValue(bytes, forType);
        } catch (final IOException e) {
            throw new SerializationException(e);
        }
    }
}
