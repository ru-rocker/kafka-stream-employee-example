package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@JsonDeserialize(builder = EmploymentHistoryDto.Builder.class)
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmploymentHistoryDto {

    @JsonProperty("emp_hist_id")
    private final Integer empHistId;

    @JsonProperty("emp_id")
    private final Integer empId;

    @JsonProperty("employer_name")
    private final String employerName;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
    }

}
