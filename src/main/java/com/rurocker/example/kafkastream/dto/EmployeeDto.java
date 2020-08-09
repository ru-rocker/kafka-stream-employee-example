package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmployeeDto {

    @JsonProperty("emp_id")
    private Integer empId;

    @JsonProperty("emp_name")
    private String empName;

    @JsonProperty("dept_id")
    private Integer deptId;
}
