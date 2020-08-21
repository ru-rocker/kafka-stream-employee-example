package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.PaymentAggregationDto;
import com.rurocker.example.kafkastream.dto.PaymentDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class PaymentAggregationSerde extends Serdes.WrapperSerde<PaymentAggregationDto> {

    public PaymentAggregationSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(PaymentAggregationDto.class));
    }
}
