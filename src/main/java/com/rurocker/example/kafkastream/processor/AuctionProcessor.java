package com.rurocker.example.kafkastream.processor;

import com.rurocker.example.kafkastream.dto.AuctionDto;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * Processor for auction streams.
 * The key is item name, and the value is {@link AuctionDto}
 * <br>
 *
 * If the current bid is higher than prev bid, then put the current bid into the key store and forward to output topic.
 *
 * @author ru-rocker
 * Created on 06-Dec-2020 8:07 PM
 */
public class AuctionProcessor implements Processor<String, AuctionDto> {

    private KeyValueStore<String, AuctionDto> kvStore;
    private ProcessorContext context;

    @SuppressWarnings("unchecked")
    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        kvStore = (KeyValueStore<String, AuctionDto>) context.getStateStore("action-key-store");
    }

    @Override
    public void process(String key, AuctionDto current) {
        final AuctionDto prev = kvStore.get(key);
        if(prev == null || prev.getBidPrice().compareTo(current.getBidPrice()) < 0 ) {
            kvStore.put(key, current);
            context.forward(key, current);
        }

    }

    @Override
    public void close() {

    }
}
