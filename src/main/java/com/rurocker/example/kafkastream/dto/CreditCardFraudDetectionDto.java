package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

/**
 * @author ru-rocker
 * Created on 11-Dec-2020 1:31 PM
 */
@ToString
@EqualsAndHashCode
@Getter
@JsonDeserialize(builder = CreditCardFraudDetectionDto.Builder.class)
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardFraudDetectionDto {

    private final String fraudFlag;
    private final Set<CreditCardTransactionDto> suspiciousTransactions;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
    }

}
