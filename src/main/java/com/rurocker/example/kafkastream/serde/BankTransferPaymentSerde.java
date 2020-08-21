package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.BankTransferPaymentDto;
import com.rurocker.example.kafkastream.dto.DepartmentDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class BankTransferPaymentSerde extends Serdes.WrapperSerde<BankTransferPaymentDto> {

    public BankTransferPaymentSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(BankTransferPaymentDto.class));
    }
}
