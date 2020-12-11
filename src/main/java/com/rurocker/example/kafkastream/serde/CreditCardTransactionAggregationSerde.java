package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.CreditCardFraudDetectionDto;
import com.rurocker.example.kafkastream.dto.CreditCardTransactionAggregationDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class CreditCardTransactionAggregationSerde extends Serdes.WrapperSerde<CreditCardTransactionAggregationDto> {

    public CreditCardTransactionAggregationSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(CreditCardTransactionAggregationDto.class));
    }
}
