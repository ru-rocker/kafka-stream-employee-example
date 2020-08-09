package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.EmployeeDto;
import com.rurocker.example.kafkastream.dto.EmploymentHistoryAggregationDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class EmploymentHistoryAggregationSerde extends Serdes.WrapperSerde<EmploymentHistoryAggregationDto> {

    public EmploymentHistoryAggregationSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(EmploymentHistoryAggregationDto.class));
    }
}
