package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class EmploymentHistoryAggregationDto {

    private Integer empId;
    private List<String> employmentHistory = new ArrayList<>();

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public List<String> getEmploymentHistory() {
        return employmentHistory;
    }

    public void add(String employment) {
        employmentHistory.add(employment);
    }
}
