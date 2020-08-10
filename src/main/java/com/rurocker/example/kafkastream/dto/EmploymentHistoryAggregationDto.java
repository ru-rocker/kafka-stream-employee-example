package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmploymentHistoryAggregationDto {

    private Integer empId;
    private Set<String> employmentHistory = new HashSet<>();

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public Set<String> getEmploymentHistory() {
        return employmentHistory;
    }

    public void add(String employment) {
        employmentHistory.add(employment);
    }
}
