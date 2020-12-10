package com.rurocker.example.kafkastream.bdd;

import io.cucumber.java8.En;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ricky Martaputra
 * Created on 10-Dec-2020 3:23 PM
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = { "classpath:features/credit-card-fraud-detection.feature" },
        glue = {"com.rurocker.example.kafkastream.bdd" },
        plugin = { "pretty" })
public class FraudDetectionStepDef implements En {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public FraudDetectionStepDef() {

        Before(scenario -> init());

        Given("^Customer has a credit card with account number \"([^\"]*)\"$", (String cc) -> {
            logger.info("Credit card no {}", cc);
        });

        When("Customer transacts ${double} at {string}", (Double amount, String iso8601str) -> {
            Instant date = Instant.parse(iso8601str);
            logger.info("Transaction with amount {} and event-date {}", amount, date);
        });

        Then("Fraud flag is {string}", (String flag) -> {
            logger.info("Fraud flag is {}", flag);
            assertThat(false).isTrue();
        });

        After(scenario -> tear());
    }

    private void init() {
        System.out.println("Instant.now() = " + Instant.now());
        System.out.println("new Date() = " + new Date());
        System.out.println("Date.from(Instant.now()) = " + Date.from(Instant.now()));
        System.out.println("before each");
    }

    private void tear() {

    }

}
