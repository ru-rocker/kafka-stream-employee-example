package com.rurocker.example.kafkastream.bdd;

import com.rurocker.example.kafkastream.dto.CreditCardFraudDetectionDto;
import com.rurocker.example.kafkastream.dto.CreditCardTransactionDto;
import com.rurocker.example.kafkastream.serde.MySerdesFactory;
import com.rurocker.example.kafkastream.topology.FraudDetectionTopology;
import io.cucumber.java8.En;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

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
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ru-rocker
 * Created on 10-Dec-2020 3:23 PM
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = { "classpath:features/credit-card-fraud-detection.feature" },
        glue = {"com.rurocker.example.kafkastream.bdd" },
        plugin = { "pretty" })
public class FraudDetectionStepDef implements En {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, CreditCardTransactionDto> transactionTopic;
    private TestOutputTopic<String, CreditCardFraudDetectionDto> fraudTopic;
    private final Serde<String> stringSerde = new Serdes.StringSerde();

    private String topicKey;
    private Integer trxId = 0;
    private CreditCardFraudDetectionDto fraudDetectionDto;

    public FraudDetectionStepDef() {

        Before(scenario -> init());

        Given("^Customer has a credit card with account number \"([^\"]*)\"$", (String cc) -> {
            logger.info("Credit card no {}", cc);
            this.topicKey = cc;
        });

        When("Customer transacts ${double} at {string}", (Double amount, String iso8601str) -> {
            Instant date = Instant.parse(iso8601str);
            logger.info("Transaction with amount {} and event-date {}", amount, date);

            final CreditCardTransactionDto transaction = getTransaction(amount, date);
            transactionTopic.pipeInput(topicKey, transaction, date);
        });

        Then("Fraud flag is {string}", (String flag) -> {
            logger.info("Fraud flag is {}", flag);
            KeyValue<String, CreditCardFraudDetectionDto> kv = null;
            while(!fraudTopic.isEmpty()) {
                kv = fraudTopic.readKeyValue();
            }

            if(kv != null) {
                assertThat(kv.key).isEqualTo(topicKey);
                fraudDetectionDto = kv.value;
                assertThat(fraudDetectionDto.getFraudFlag()).isEqualTo(flag);
            } else {
                assertThat(flag).isNullOrEmpty();
            }
        });

        And("total suspicious amount is ${double}", (Double suspicious) -> {
            logger.info("suspicious amount is {}", suspicious);
            if(fraudDetectionDto != null) {
                final Set<CreditCardTransactionDto> list = fraudDetectionDto.getSuspiciousTransactions();
                final BigDecimal total = list.stream()
                        .map(value -> BigDecimal.valueOf(value.getTrxAmount()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                assertThat(total.doubleValue()).isEqualTo(suspicious);
            } else {
                assertThat(suspicious).isEqualTo(0.0);
            }
        });

        After(scenario -> tear());
    }

    private void init() {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "app-id");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());

        FraudDetectionTopology fraudDetectionTopology = new FraudDetectionTopology();
        final StreamsBuilder builder = new StreamsBuilder();
        fraudDetectionTopology.createTopology(builder);
        final Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);
        transactionTopic = testDriver.createInputTopic("credit-card-transaction-input", stringSerde.serializer(),
                MySerdesFactory.creditCardTransactionSerde().serializer());
        fraudTopic = testDriver.createOutputTopic("credit-card-fraud-detection-output", stringSerde.deserializer(),
                MySerdesFactory.creditCardFraudDetectionSerde().deserializer());

    }

    private CreditCardTransactionDto getTransaction(Double amount, Instant trxDate) {
        return CreditCardTransactionDto.builder()
                .creditCardHolder("Alice")
                .creditCardNo(topicKey)
                .merchantId(new Random().nextInt(100))
                .trxId(trxId++)
                .trxAmount(amount)
                .trxDate(LocalDateTime.ofInstant(trxDate, ZoneOffset.UTC))
                .build();
    }

    private void tear() throws IOException {
        testDriver.close();
        FileUtils.deleteDirectory(new File("/tmp/kafka-streams/app-id"));
    }

}
