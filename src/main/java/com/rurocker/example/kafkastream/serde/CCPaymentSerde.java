package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.CCPaymentDto;
import com.rurocker.example.kafkastream.dto.DepartmentDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class CCPaymentSerde extends Serdes.WrapperSerde<CCPaymentDto> {

    public CCPaymentSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(CCPaymentDto.class));
    }
}
