package com.rurocker.example.kafkastream.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;

import java.time.Duration;
import java.util.Arrays;

/**
 * Word count topology with time windows
 * @author ru-rocker
 * Created on 06-Dec-2020 7:29 PM
 */
public class WordCountTimeWindowsTopology {

    public void createTopology(StreamsBuilder builder) {
        final KStream<String, String> textLines = builder
                .stream("streams-plaintext-input",
                        Consumed.with(Serdes.String(), Serdes.String()));

        textLines
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                .groupBy((key, value) -> value)
                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(5)))
                .count(Materialized.as("WordCount"))
                .toStream()
                .peek((key,value) -> System.out.println("(Windows) key,value = " + key.window()  + "," + value))
                .map((key, value) -> new KeyValue<>(key.key(),value))
                .peek((key,value) -> System.out.println("(Word) key,value = " + key  + "," + value))
                .to("streams-wordcount-output",
                        Produced.with(Serdes.String(), Serdes.Long()));
    }
}
