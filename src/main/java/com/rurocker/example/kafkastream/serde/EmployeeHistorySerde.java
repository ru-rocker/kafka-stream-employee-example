package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.EmploymentHistoryDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class EmployeeHistorySerde extends Serdes.WrapperSerde<EmploymentHistoryDto> {

    public EmployeeHistorySerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(EmploymentHistoryDto.class));
    }
}
