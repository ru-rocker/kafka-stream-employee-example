package com.rurocker.example.kafkastream.processor;

import com.rurocker.example.kafkastream.dto.AuctionDto;
import com.rurocker.example.kafkastream.serde.MySerdesFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mock processor
 *
 * @author ru-rocker
 * Created on 06-Dec-2020 8:21 PM
 */
public class AuctionProcessorTest {

    private AuctionProcessor processor;
    private MockProcessorContext context;
    private KeyValueStore<String, AuctionDto> store;

    @BeforeEach
    public void init() {
        processor = new AuctionProcessor();

        final Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "unit-test");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MySerdesFactory.auctionSerde().getClass());

        context = new MockProcessorContext(config);
        store = Stores
                .keyValueStoreBuilder(Stores.inMemoryKeyValueStore("action-key-store"),
                        Serdes.String(), MySerdesFactory.auctionSerde())
                .withLoggingDisabled().build();
        store.init(context, store);
        context.register(store, null);
        processor.init(context);
    }

    @Test
    public void testInitialBid_then_ReturnTheBid() {

        AuctionDto auctionDto = AuctionDto.builder()
                .customerName("Alice")
                .bidPrice(20.0)
                .build();

        processor.process("item-1", auctionDto);
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();

        // confirmed that processor forwards the first bid
        MockProcessorContext.CapturedForward forward = forwarded.next();
        assertThat(forward.keyValue()).isEqualTo(new KeyValue<>("item-1", auctionDto));
        assertThat(forwarded.hasNext()).isFalse();

        // keyStore in processor now contains item-1 with Alice as the bidder with 20.0
        assertThat(store.get("item-1")).isEqualTo(auctionDto);
    }

    @Test
    public void testLowerBid_then_ReturnPrevBid() {

        AuctionDto existingAuction = AuctionDto.builder()
                .customerName("Alice")
                .bidPrice(20.0)
                .build();
        // simulate existing auction
        store.put("item-1", existingAuction);

        AuctionDto newAuction = AuctionDto.builder()
                .customerName("Bob")
                .bidPrice(10.0)
                .build();

        processor.process("item-1", newAuction);
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();

        // confirmed that processor does not forward anything
        assertThat(forwarded.hasNext()).isFalse();

        // keyStore in processor contains item-1 with existing
        assertThat(store.get("item-1")).isEqualTo(existingAuction);
    }

    @Test
    public void testSameBid_then_ReturnPrevBid() {

        AuctionDto existingAuction = AuctionDto.builder()
                .customerName("Alice")
                .bidPrice(20.0)
                .build();
        // simulate existing auction
        store.put("item-1", existingAuction);

        AuctionDto newAuction = AuctionDto.builder()
                .customerName("Bob")
                .bidPrice(20.0)
                .build();

        processor.process("item-1", newAuction);
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();

        // confirmed that processor does not forward anything
        assertThat(forwarded.hasNext()).isFalse();

        // keyStore in processor contains item-1 with existing
        assertThat(store.get("item-1")).isEqualTo(existingAuction);
    }


    @Test
    public void testHigherBid_then_ReturnNewBid() {

        AuctionDto existingAuction = AuctionDto.builder()
                .customerName("Alice")
                .bidPrice(20.0)
                .build();
        // simulate existing auction
        store.put("item-1", existingAuction);

        AuctionDto newAuction = AuctionDto.builder()
                .customerName("Bob")
                .bidPrice(25.0)
                .build();

        processor.process("item-1", newAuction);
        final Iterator<MockProcessorContext.CapturedForward> forwarded = context.forwarded().iterator();

        // confirmed that processor forwards the newest bid
        MockProcessorContext.CapturedForward forward = forwarded.next();
        assertThat(forward.keyValue()).isEqualTo(new KeyValue<>("item-1", newAuction));
        assertThat(forwarded.hasNext()).isFalse();

        // keyStore in processor contains item-1 with existing
        assertThat(store.get("item-1")).isEqualTo(newAuction);
    }
}
