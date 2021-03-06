package com.rurocker.example.kafkastream.topology;

import org.apache.commons.io.FileUtils;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ru-rocker
 * Created on 06-Dec-2020 2:48 PM
 */
public class WordCountTimeWindowsTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> plainTextInput;
    private TestOutputTopic<String, Long> wordCountOutput;
    private final Serde<String> stringSerde = new Serdes.StringSerde();
    private final Serde<Long> longSerde = new Serdes.LongSerde();

    @BeforeEach
    public void init() {

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "app-id");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());

        WordCountTimeWindowsTopology wordCountTopology = new WordCountTimeWindowsTopology();
        final StreamsBuilder builder = new StreamsBuilder();
        wordCountTopology.createTopology(builder);
        final Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);
        plainTextInput = testDriver.createInputTopic("streams-plaintext-input", stringSerde.serializer(),
                stringSerde.serializer());
        wordCountOutput = testDriver.createOutputTopic("streams-wordcount-output", stringSerde.deserializer(),
                longSerde.deserializer());
    }

    @Test
    @DisplayName("Test word count streams")
    public void testWordCountStream() {
        String text1 = "Welcome to kafka streams";
        String text2 = "Kafka streams is great";
        String text3 = "Welcome back";

        // expected output
        Map<String,Long> expected = Map.of(
                // please take note, now the welcome is only 1.
                // Because the second welcome word, come after 5 minutes duration.
                "welcome", 1L,
                "to", 1L,
                "kafka", 2L,
                "streams", 2L,
                "is", 1L,
                "great", 1L,
                "back", 1L
        );

        final Instant now = Instant.now();

        // insert two lines with the same timestamp
        plainTextInput.pipeInput(null,text1, now);
        plainTextInput.pipeInput(null,text2, now);

        // simulate 5 minutes after the first two
        plainTextInput.pipeInput(null,text3, now.plus(5, ChronoUnit.MINUTES));

        assertThat(wordCountOutput.isEmpty()).isFalse();

        // result
        Map<String, Long> result = new HashMap<>();
        while(!wordCountOutput.isEmpty()) {
            final KeyValue<String, Long> kv = wordCountOutput.readKeyValue();
            result.put(kv.key, kv.value);
        }
        assertThat(result).containsExactlyInAnyOrderEntriesOf(expected);

        assertThat(wordCountOutput.isEmpty()).isTrue();
    }

    @AfterEach
    public void tearDown() throws IOException {
        testDriver.close();
        FileUtils.deleteDirectory(new File("/tmp/kafka-streams/app-id"));
    }

}
