package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.DepartmentDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class DepartmentSerde extends Serdes.WrapperSerde<DepartmentDto> {

    public DepartmentSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(DepartmentDto.class));
    }
}
