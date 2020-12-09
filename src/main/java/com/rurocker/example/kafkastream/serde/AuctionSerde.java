package com.rurocker.example.kafkastream.serde;

import com.rurocker.example.kafkastream.dto.AuctionDto;
import com.rurocker.example.kafkastream.dto.EmployeeDto;
import com.rurocker.example.kafkastream.serde.json.MyJsonDeserializer;
import com.rurocker.example.kafkastream.serde.json.MyJsonSerializer;
import org.apache.kafka.common.serialization.Serdes;

public class AuctionSerde extends Serdes.WrapperSerde<AuctionDto> {

    public AuctionSerde() {
        super(new MyJsonSerializer<>(), new MyJsonDeserializer<>(AuctionDto.class));
    }
}
