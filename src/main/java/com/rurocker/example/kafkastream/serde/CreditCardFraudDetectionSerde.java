package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.CreditCardFraudDetectionDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class CreditCardFraudDetectionSerde extends Serdes.WrapperSerde<CreditCardFraudDetectionDto> {

    public CreditCardFraudDetectionSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(CreditCardFraudDetectionDto.class));
    }
}
