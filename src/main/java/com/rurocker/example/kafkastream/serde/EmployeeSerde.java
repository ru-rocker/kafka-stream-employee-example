package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.DepartmentDto;
import com.rurocker.example.kafkastream.dto.EmployeeDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class EmployeeSerde extends Serdes.WrapperSerde<EmployeeDto> {

    public EmployeeSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(EmployeeDto.class));
    }
}
