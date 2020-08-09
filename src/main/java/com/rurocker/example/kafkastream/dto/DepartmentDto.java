package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DepartmentDto {

    @JsonProperty("dept_id")
    private Integer deptId;

    @JsonProperty("dept_name")
    private String deptName;
}
