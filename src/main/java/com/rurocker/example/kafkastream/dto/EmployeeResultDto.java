package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EmployeeResultDto {

    @JsonProperty("emp_id")
    private Integer empId;

    @JsonProperty("emp_name")
    private String empName;

    @JsonProperty("dept_id")
    private Integer deptId;

    @JsonProperty("dept_name")
    private String deptName;

    @JsonProperty("employment_history")
    private List<String> employmentHistory;
}
