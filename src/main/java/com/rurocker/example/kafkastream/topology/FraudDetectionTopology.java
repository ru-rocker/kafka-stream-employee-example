package com.rurocker.example.kafkastream.topology;

import com.rurocker.example.kafkastream.dto.CreditCardFraudDetectionDto;
import com.rurocker.example.kafkastream.dto.CreditCardTransactionAggregationDto;
import com.rurocker.example.kafkastream.dto.CreditCardTransactionDto;
import com.rurocker.example.kafkastream.serde.CreditCardTransactionSerde;
import com.rurocker.example.kafkastream.serde.MySerdesFactory;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an example to detect if a credit card transactions exceed
 * certain limit during specific period.
 *
 * User story:
 * As a credit card owner with number XXX, I want to be notified if my transactions
 * exceed $1000 in the 5 minutes duration.
 *
 * See gerkhin files in features/credit-card-fraud-detection.feature for more details.
 *
 * @author ru-rocker
 * Created on 06-Dec-2020 7:21 PM
 */
public class FraudDetectionTopology {

    public static final String CREDIT_CARD_TRANSACTION_INPUT = "credit-card-transaction-input";
    public static final String SINGLE_TRANSACTION_FRAUD_DETECTION_RESULT = "single-transaction-fraud-detection-result";
    public static final String HOPPING_WINDOWS_TRANSACTION_FRAUD_DETECTION_RESULT = "hopping-windows-transaction-fraud-detection-result";
    public static final String CREDIT_CARD_FRAUD_DETECTION_OUTPUT = "credit-card-fraud-detection-output";

    public void createTopology(StreamsBuilder builder) {

        final Double singleThreshold = 1000.0;
        final Double hoppingWindowThreshold = 1500.0;

        final Serde<String> keySerde = Serdes.String();
        final CreditCardTransactionSerde valueSerde = MySerdesFactory.creditCardTransactionSerde();

        final KStream<String, CreditCardTransactionDto> input =
                builder.stream(CREDIT_CARD_TRANSACTION_INPUT, Consumed.with(keySerde, valueSerde));

        input.filter((key, value) -> singleThreshold.compareTo(value.getTrxAmount()) < 0)
            .mapValues(value -> CreditCardFraudDetectionDto.builder()
                    .fraudFlag("Y")
                    .suspiciousTransactions(List.of(value))
                    .build())
            .to(SINGLE_TRANSACTION_FRAUD_DETECTION_RESULT,
                    Produced.with(keySerde, MySerdesFactory.creditCardFraudDetectionSerde()));

        input.groupByKey(Grouped.with(keySerde, valueSerde))
            .windowedBy(TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(1)))
            .aggregate(() -> CreditCardTransactionAggregationDto.builder().ongoingTransactions(List.of()).build(),
                    (key, value, aggr) -> {
                        final List<CreditCardTransactionDto> current = aggr.getOngoingTransactions();
                        List<CreditCardTransactionDto> list = new ArrayList<>(current);
                        list.add(value);
                        return aggr.toBuilder()
                                .ongoingTransactions(list)
                                .build();
                    },
                    Materialized.with(keySerde, MySerdesFactory.creditCardTransactionAggregationSerde()))
            .toStream()
            .filter((key, value) -> hoppingWindowThreshold.compareTo(value.sumOngoingTransactions()) < 0)
            .mapValues(value -> CreditCardFraudDetectionDto.builder()
                    .fraudFlag("Y")
                    .suspiciousTransactions(value.getOngoingTransactions())
                    .build())
            .filter((key, value) -> value != null)
            .map((key,value) -> new KeyValue<>(key.key(), value))
            .to(HOPPING_WINDOWS_TRANSACTION_FRAUD_DETECTION_RESULT,
                    Produced.with(keySerde, MySerdesFactory.creditCardFraudDetectionSerde()));

    }
}