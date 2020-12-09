package com.rurocker.example.kafkastream.processor;

import com.rurocker.example.kafkastream.dto.AuctionDto;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;

/**
 * Processor for auction streams with schedule punctuation WALL_CLOCK.
 * The key is item name, and the value is {@link AuctionDto}
 * <br>
 *
 * If the current bid is higher than prev bid, then put the current bid into the key store and forward to output topic.
 *
 * @author ru-rocker
 * Created on 06-Dec-2020 8:07 PM
 */
public class AuctionWithScheduleWallClockProcessor implements Processor<String, AuctionDto> {

    private KeyValueStore<String, AuctionDto> kvStore;
    private ProcessorContext context;

    @SuppressWarnings("unchecked")
    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        kvStore = (KeyValueStore<String, AuctionDto>) context.getStateStore("action-key-store-schedule-wall-clock");
        this.context.schedule(Duration.ofMillis(1000), PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
            final KeyValueIterator<String, AuctionDto> iter = this.kvStore.all();
            while (iter.hasNext()) {
                final KeyValue<String, AuctionDto> entry = iter.next();
                context.forward(entry.key, entry.value);
            }
            iter.close();

            // commit the current processing progress
            context.commit();
        });
    }

    @Override
    public void process(String key, AuctionDto current) {
        final AuctionDto prev = kvStore.get(key);
        if(prev == null || prev.getBidPrice().compareTo(current.getBidPrice()) < 0 ) {
            kvStore.put(key, current);
        }

    }

    @Override
    public void close() {

    }
}
