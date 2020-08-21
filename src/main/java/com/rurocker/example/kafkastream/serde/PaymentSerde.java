package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.CCPaymentDto;
import com.rurocker.example.kafkastream.dto.PaymentDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class PaymentSerde extends Serdes.WrapperSerde<PaymentDto> {

    public PaymentSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(PaymentDto.class));
    }
}
