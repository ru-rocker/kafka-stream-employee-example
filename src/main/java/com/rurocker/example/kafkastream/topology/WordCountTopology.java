package com.rurocker.example.kafkastream.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;

/**
 * @author ru-rocker
 * Created on 06-Dec-2020 2:45 PM
 */
public class WordCountTopology {

    public void createTopology(StreamsBuilder builder) {
        final KStream<String, String> textLines = builder
                .stream("streams-plaintext-input",
                        Consumed.with(Serdes.String(), Serdes.String()));

        textLines
            .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
            .groupBy((key, value) -> value)
            .count(Materialized.as("WordCount"))
            .toStream()
            .to("streams-wordcount-output",
                    Produced.with(Serdes.String(), Serdes.Long()));
    }
}
