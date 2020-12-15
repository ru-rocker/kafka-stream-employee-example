package com.rurocker.example.kafkastream.topology;

import com.rurocker.example.kafkastream.dto.CreditCardFraudDetectionDto;
import com.rurocker.example.kafkastream.dto.CreditCardTransactionAggregationDto;
import com.rurocker.example.kafkastream.dto.CreditCardTransactionDto;
import com.rurocker.example.kafkastream.serde.CreditCardFraudDetectionSerde;
import com.rurocker.example.kafkastream.serde.CreditCardTransactionSerde;
import com.rurocker.example.kafkastream.serde.MySerdesFactory;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.SessionWindows;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.ValueJoiner;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

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
    public static final String CREDIT_CARD_FRAUD_DETECTION_OUTPUT = "credit-card-fraud-detection-output";

    public void createTopology(StreamsBuilder builder) {

        final Double singleThreshold = 1000.0;
        final Double hoppingWindowThreshold = 1500.0;
        final Double sessionWindowThreshold = 4000.0;

        final Serde<String> keySerde = Serdes.String();
        final CreditCardTransactionSerde creditCardTransactionSerde = MySerdesFactory.creditCardTransactionSerde();
        final CreditCardFraudDetectionSerde creditCardFraudDetectionSerde =
                MySerdesFactory.creditCardFraudDetectionSerde();

        final KStream<String, CreditCardTransactionDto> input =
                builder.stream(CREDIT_CARD_TRANSACTION_INPUT, Consumed.with(keySerde, creditCardTransactionSerde));

        // single
        final KStream<String, CreditCardFraudDetectionDto> single =
            input.filter((key, value) -> singleThreshold.compareTo(value.getTrxAmount()) < 0)
                .mapValues(value -> CreditCardFraudDetectionDto.builder()
                    .fraudFlag("Y")
                    .suspiciousTransactions(Set.of(value))
                    .build());

        // hopping-windows
        final KStream<String, CreditCardFraudDetectionDto> hopping =
                input.groupByKey(Grouped.with(keySerde, creditCardTransactionSerde))
                    .windowedBy(TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofMinutes(1)))
                    .aggregate(() -> CreditCardTransactionAggregationDto.builder().ongoingTransactions(Set.of()).build(),
                            (key, value, aggr) -> {
                                final Set<CreditCardTransactionDto> current = aggr.getOngoingTransactions();
                                Set<CreditCardTransactionDto> set = new HashSet<>(current);
                                set.add(value);
                                return aggr.toBuilder()
                                        .ongoingTransactions(set)
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
                    .map((key,value) -> new KeyValue<>(key.key(), value));

        // session-windows
        final KStream<String, CreditCardFraudDetectionDto> session =
                input.groupByKey(Grouped.with(keySerde, creditCardTransactionSerde))
                    .windowedBy(SessionWindows.with(Duration.ofHours(1)))
                    .aggregate(() -> CreditCardTransactionAggregationDto.builder().ongoingTransactions(Set.of()).build(),
                            (key, value, aggr) -> {
                                final Set<CreditCardTransactionDto> current = aggr.getOngoingTransactions();
                                Set<CreditCardTransactionDto> set = new HashSet<>(current);
                                set.add(value);
                                return aggr.toBuilder()
                                        .ongoingTransactions(set)
                                        .build();
                            },
                            (key, aggOne, aggTwo) -> {
                                final Set<CreditCardTransactionDto> ongoing1 = aggOne.getOngoingTransactions();
                                final Set<CreditCardTransactionDto> ongoing2 = aggTwo.getOngoingTransactions();
                                Set<CreditCardTransactionDto> set = new HashSet<>(ongoing1);
                                set.addAll(ongoing2);
                                return aggOne.toBuilder().ongoingTransactions(set).build();
                            },
                            Materialized.with(keySerde, MySerdesFactory.creditCardTransactionAggregationSerde()))
                    .toStream()
                    .filter((key, value) -> sessionWindowThreshold.compareTo(value.sumOngoingTransactions()) < 0)
                    .mapValues(value -> CreditCardFraudDetectionDto.builder()
                            .fraudFlag("Y")
                            .suspiciousTransactions(value.getOngoingTransactions())
                            .build())
                    .filter((key, value) -> value != null)
                    .map((key,value) -> new KeyValue<>(key.key(), value));

        // always suspicious trx going into these joins
        single.outerJoin(hopping,
                valueJoiner(),
                JoinWindows.of(Duration.ofSeconds(1)),
                StreamJoined.with(keySerde, creditCardFraudDetectionSerde, creditCardFraudDetectionSerde))
            .outerJoin(session,
                valueJoiner(),
                JoinWindows.of(Duration.ofSeconds(1)),
                StreamJoined.with(keySerde, creditCardFraudDetectionSerde, creditCardFraudDetectionSerde))
            .to(CREDIT_CARD_FRAUD_DETECTION_OUTPUT,
                    Produced.with(keySerde, creditCardFraudDetectionSerde));

    }

    private ValueJoiner<CreditCardFraudDetectionDto, CreditCardFraudDetectionDto,
            CreditCardFraudDetectionDto> valueJoiner() {

        return (v1, v2) -> {
            Set<CreditCardTransactionDto> result = new HashSet<>();
            if (v1 != null && v1.getFraudFlag().equals("Y")) {
                result.addAll(v1.getSuspiciousTransactions());
            }
            if (v2 != null && v2.getFraudFlag().equals("Y")) {
                result.addAll(v2.getSuspiciousTransactions());
            }
            return CreditCardFraudDetectionDto.builder()
                    .fraudFlag("Y")
                    .suspiciousTransactions(result)
                    .build();
        };
    }
}