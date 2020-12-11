package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author ru-rocker
 * Created on 11-Dec-2020 4:06 PM
 */
@ToString
@EqualsAndHashCode
@Getter
@JsonDeserialize(builder = CreditCardTransactionAggregationDto.Builder.class)
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardTransactionAggregationDto {

    private final List<CreditCardTransactionDto> ongoingTransactions;

    public Double sumOngoingTransactions() {
        return ongoingTransactions.stream()
                .map(value -> BigDecimal.valueOf(value.getTrxAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
    }

}
