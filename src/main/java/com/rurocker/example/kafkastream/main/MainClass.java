package com.rurocker.example.kafkastream.main;

import com.rurocker.example.kafkastream.topology.EmployeeTopology;
import com.rurocker.example.kafkastream.topology.PaymentTopology;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class MainClass {

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger("ru-rocker-main-class");

        final StreamsBuilder builder = new StreamsBuilder();

        // Kafka Stream Properties
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ru-rocker-abc");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // disable cache. DEV only
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        // create employee topology
//        EmployeeTopology employeeTopology = new EmployeeTopology();
//        employeeTopology.createTopology(builder);

        // create payment topology
        PaymentTopology paymentTopology = new PaymentTopology();
        paymentTopology.createTopology(builder);

        // build topology
        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        // clean up existing stream (DEV only)
        streams.cleanUp();

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("ru-rocker-kafka-stream") {
            @Override
            public void run() {
                logger.info("Shutting down stream...");
                streams.close();
                logger.info("Stream is stopped");
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Exception e) {
            logger.error("Exception: ", e);
            System.exit(1);
        }
        System.exit(0);
    }
}
