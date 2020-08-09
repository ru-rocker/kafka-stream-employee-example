package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmploymentHistoryDto {

    @JsonProperty("emp_hist_id")
    private Integer empHistId;

    @JsonProperty("emp_id")
    private Integer empId;

    @JsonProperty("employer_name")
    private String employerName;
}
