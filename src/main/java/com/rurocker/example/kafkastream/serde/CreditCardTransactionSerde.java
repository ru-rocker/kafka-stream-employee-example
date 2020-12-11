package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.CreditCardTransactionDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class CreditCardTransactionSerde extends Serdes.WrapperSerde<CreditCardTransactionDto> {

    public CreditCardTransactionSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(CreditCardTransactionDto.class));
    }
}
