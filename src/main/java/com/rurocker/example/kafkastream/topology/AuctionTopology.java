package com.rurocker.example.kafkastream.topology;

import com.rurocker.example.kafkastream.dto.AuctionDto;
import com.rurocker.example.kafkastream.processor.AuctionProcessor;
import com.rurocker.example.kafkastream.processor.AuctionWithScheduleStreamTimeProcessor;
import com.rurocker.example.kafkastream.processor.AuctionWithScheduleWallClockProcessor;
import com.rurocker.example.kafkastream.serde.MySerdesFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

/**
 * Topology for auction stream.
 * <br>
 *
 * @author ru-rocker
 * Created on 06-Dec-2020 8:41 PM
 */
public class AuctionTopology {

    public void createCustomProcessor(final Topology topology) {
        final StoreBuilder<KeyValueStore<String, AuctionDto>> auctionKeyStore = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("action-key-store"), Serdes.String(),
                MySerdesFactory.auctionSerde());
        final StoreBuilder<KeyValueStore<String, AuctionDto>> auctionWallClockKeyStore = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("action-key-store-schedule-wall-clock"), Serdes.String(),
                MySerdesFactory.auctionSerde());
        final StoreBuilder<KeyValueStore<String, AuctionDto>> auctionStreamTimeKeyStore = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("action-key-store-schedule-stream-time"), Serdes.String(),
                MySerdesFactory.auctionSerde());

        topology.addSource("source", Serdes.String().deserializer(),
                MySerdesFactory.auctionSerde().deserializer(), "auction-bid-input");
        topology.addProcessor("proc", AuctionProcessor::new, "source");
        topology.addStateStore(auctionKeyStore, "proc");
        topology.addSink("sink", "auction-bid-output", "proc");

        topology.addProcessor("proc-stream-time", AuctionWithScheduleStreamTimeProcessor::new, "source");
        topology.addStateStore(auctionStreamTimeKeyStore, "proc-stream-time");
        topology.addSink("sink-stream-time", "auction-bid-stream-time-output", "proc-stream-time");

        topology.addProcessor("proc-wall-clock", AuctionWithScheduleWallClockProcessor::new, "source");
        topology.addStateStore(auctionWallClockKeyStore, "proc-wall-clock");
        topology.addSink("sink-wall-clock", "auction-bid-wall-clock-output", "proc-wall-clock");
    }

}
