package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.EmployeeResultDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class EmployeeResultSerde extends Serdes.WrapperSerde<EmployeeResultDto> {

    public EmployeeResultSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(EmployeeResultDto.class));
    }
}
