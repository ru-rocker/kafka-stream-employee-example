package com.rurocker.example.kafkastream.topology;

import com.rurocker.example.kafkastream.dto.AuctionDto;
import com.rurocker.example.kafkastream.serde.MySerdesFactory;
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
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ru-rocker
 * Created on 06-Dec-2020 20:48 PM
 */
public class AuctionTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, AuctionDto> inputTopic;
    private TestOutputTopic<String, AuctionDto> outputTopic;
    private TestOutputTopic<String, AuctionDto> outputWallClockTopic;
    private TestOutputTopic<String, AuctionDto> outputStreamTimeTopic;
    private final Serde<String> stringSerde = new Serdes.StringSerde();

    @BeforeEach
    public void init() {

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "app-id");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MySerdesFactory.auctionSerde().getClass().getName());

        AuctionTopology auctionTopology = new AuctionTopology();
        final StreamsBuilder builder = new StreamsBuilder();
        final Topology topology = builder.build();
        auctionTopology.createCustomProcessor(topology);

        testDriver = new TopologyTestDriver(topology, props);
        inputTopic = testDriver.createInputTopic("auction-bid-input", stringSerde.serializer(),
                MySerdesFactory.auctionSerde().serializer());
        outputTopic = testDriver.createOutputTopic("auction-bid-output", stringSerde.deserializer(),
                MySerdesFactory.auctionSerde().deserializer());
        outputStreamTimeTopic = testDriver.createOutputTopic("auction-bid-stream-time-output", stringSerde.deserializer(),
                MySerdesFactory.auctionSerde().deserializer());
        outputWallClockTopic = testDriver.createOutputTopic("auction-bid-wall-clock-output", stringSerde.deserializer(),
                MySerdesFactory.auctionSerde().deserializer());
    }

    @Test
    @DisplayName("Test multiple auction")
    public void testAuction() {

        AuctionDto alice = AuctionDto.builder()
                .customerName("Alice")
                .bidPrice(10.0)
                .build();

        AuctionDto bob = AuctionDto.builder()
                .customerName("Bob")
                .bidPrice(9.5)
                .build();

        AuctionDto charlie = AuctionDto.builder()
                .customerName("Charlie")
                .bidPrice(10.5)
                .build();

        AuctionDto dave = AuctionDto.builder()
                .customerName("Dave")
                .bidPrice(1.5)
                .build();

        inputTopic.pipeInput("item-1", alice);
        inputTopic.pipeInput("item-1", bob);
        inputTopic.pipeInput("item-1", charlie);
        inputTopic.pipeInput("item-2", dave);

        Map<String, AuctionDto> expected = Map.of(
                "item-1", charlie,
                "item-2", dave
        );

        // make sure the output is not empty
        assertThat(outputTopic.isEmpty()).isFalse();
        Map<String, AuctionDto> result = new HashMap<>();
        while(!outputTopic.isEmpty()){
            final KeyValue<String, AuctionDto> kv = outputTopic.readKeyValue();
            result.put(kv.key, kv.value);
        }

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expected);
    }

    /**
     * If at least one partition does not have any new data available,
     * stream-time will not be advanced and thus punctuate() will not be triggered if
     * PunctuationType.STREAM_TIME was specified.
     *
     * Since the testDriver is a single-partitioned, it is okay to say every time the input topic retrieve new data,
     * the punctuate is triggered by advancing event time.
     *
     * https://kafka.apache.org/10/documentation/streams/developer-guide/processor-api.html
     */
    @Test
    @DisplayName("Test multiple auction with stream time")
    public void testAuctionWithStreamTime() {

        AuctionDto alice = AuctionDto.builder()
                .customerName("Alice")
                .bidPrice(10.0)
                .build();

        AuctionDto bob = AuctionDto.builder()
                .customerName("Bob")
                .bidPrice(9.5)
                .build();

        AuctionDto charlie = AuctionDto.builder()
                .customerName("Charlie")
                .bidPrice(10.5)
                .build();

        AuctionDto dave = AuctionDto.builder()
                .customerName("Dave")
                .bidPrice(1.5)
                .build();

        Instant now = Instant.now();
        inputTopic.pipeInput("item-1", alice, now);
        inputTopic.pipeInput("item-1", bob, now.plus(1, ChronoUnit.SECONDS));
        inputTopic.pipeInput("item-1", charlie, now.plus(2, ChronoUnit.SECONDS));
        inputTopic.pipeInput("item-2", dave, now.plus(3, ChronoUnit.SECONDS));

        Map<String, AuctionDto> expected = Map.of(
                "item-1", charlie,
                "item-2", dave
        );

        // make sure the output is not empty
        assertThat(outputStreamTimeTopic.isEmpty()).isFalse();
        Map<String, AuctionDto> result = new HashMap<>();
        while(!outputStreamTimeTopic.isEmpty()){
            final KeyValue<String, AuctionDto> kv = outputStreamTimeTopic.readKeyValue();
            result.put(kv.key, kv.value);
        }

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    @DisplayName("Test multiple auction with wall clock")
    public void testAuctionWithWallClock() {

        AuctionDto alice = AuctionDto.builder()
                .customerName("Alice")
                .bidPrice(10.0)
                .build();

        AuctionDto bob = AuctionDto.builder()
                .customerName("Bob")
                .bidPrice(9.5)
                .build();

        AuctionDto charlie = AuctionDto.builder()
                .customerName("Charlie")
                .bidPrice(10.5)
                .build();

        AuctionDto dave = AuctionDto.builder()
                .customerName("Dave")
                .bidPrice(1.5)
                .build();

        inputTopic.pipeInput("item-1", alice);
        inputTopic.pipeInput("item-1", bob);
        inputTopic.pipeInput("item-1", charlie);
        inputTopic.pipeInput("item-2", dave);

        Map<String, AuctionDto> expected = Map.of(
                "item-1", charlie,
                "item-2", dave
        );

        // output topic should be empty because the wall clock time still not advancing
        assertThat(outputWallClockTopic.isEmpty()).isTrue();

        // advancing wall clock time
        testDriver.advanceWallClockTime(Duration.ofMillis(1000));

        // make sure the output is not empty
        assertThat(outputWallClockTopic.isEmpty()).isFalse();
        Map<String, AuctionDto> result = new HashMap<>();
        while(!outputWallClockTopic.isEmpty()){
            final KeyValue<String, AuctionDto> kv = outputWallClockTopic.readKeyValue();
            result.put(kv.key, kv.value);
        }

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @AfterEach
    public void tearDown() throws IOException {
        testDriver.close();
        FileUtils.deleteDirectory(new File("/tmp/kafka-streams/app-id"));
    }

}
