package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author ru-rocker
 * Created on 11-Dec-2020 1:28 PM
 */
@ToString
@EqualsAndHashCode
@Getter
@JsonDeserialize(builder = CreditCardTransactionDto.Builder.class)
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCardTransactionDto {

    private final Integer trxId;
    private final String creditCardNo;
    private final String creditCardHolder;
    private final String merchantId;
    private final Double trxAmount;

    @JsonFormat(timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime trxDate;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
    }

}
