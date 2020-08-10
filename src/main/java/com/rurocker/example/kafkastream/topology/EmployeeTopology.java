package com.rurocker.example.kafkastream.topology;

import com.rurocker.example.kafkastream.dto.DepartmentDto;
import com.rurocker.example.kafkastream.dto.EmployeeResultDto;
import com.rurocker.example.kafkastream.dto.EmployeeDto;
import com.rurocker.example.kafkastream.dto.EmploymentHistoryAggregationDto;
import com.rurocker.example.kafkastream.serde.MySerdesFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

public class EmployeeTopology {

    public void createTopology(StreamsBuilder builder) {

        // select key then convert stream into table
        final KTable<Integer, DepartmentDto> deptTable =
                builder.stream("DEPT",
                    Consumed.with(Serdes.Integer(), MySerdesFactory.departmentSerde()))
                        .map((key, value) -> new KeyValue<>(value.getDeptId(), value))
                        .toTable(Materialized.<Integer, DepartmentDto, KeyValueStore<Bytes, byte[]>>
                            as("DEPT-MV")
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(MySerdesFactory.departmentSerde()));

        final KTable<Integer, EmployeeDto> empTable =
                builder.stream("EMPLOYEE",
                    Consumed.with(Serdes.Integer(), MySerdesFactory.employeeSerde()))
                        .map((key, value) -> new KeyValue<>(value.getEmpId(), value))
                        .toTable(Materialized.<Integer, EmployeeDto, KeyValueStore<Bytes, byte[]>>
                                as("EMPLOYEE-MV")
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(MySerdesFactory.employeeSerde()));

        // N:1 join -> EMPLOYEE and DEPARTMENT
        final KTable<Integer, EmployeeResultDto> empDeptTable = empTable.join(deptTable,
                // foreignKeyExtractor. Get dept_id from employee and join with dept
                EmployeeDto::getDeptId,
                // join emp and dept, return EmployeeDataDto
                (emp, dept) -> {
                    EmployeeResultDto employeeResultDto = new EmployeeResultDto();
                    employeeResultDto.setDeptId(dept.getDeptId());
                    employeeResultDto.setDeptName(dept.getDeptName());
                    employeeResultDto.setEmpId(emp.getEmpId());
                    employeeResultDto.setEmpName(emp.getEmpName());
                    return employeeResultDto;
                },
                // store into materialized view with neam EMP-DEPT-MV
                Materialized.<Integer, EmployeeResultDto, KeyValueStore<Bytes, byte[]>>
                    as("EMP-DEPT-MV")
                        .withKeySerde(Serdes.Integer())
                        .withValueSerde(MySerdesFactory.employeeResultSerde())
        );

        // 1:N join -> EMPLOYEE_DATA and EMPLOYEMENT_HISTORY
        // a. select emp_id as key, group by key (emp_id) then aggregate the result
        final KTable<Integer, EmploymentHistoryAggregationDto> employmentHistoryAggr =
                builder.stream("EMPLOYMENT-HISTORY",
                    Consumed.with(Serdes.Integer(), MySerdesFactory.employeeHistorySerde()))
                .selectKey((key,empHist) -> empHist.getEmpId())
                .groupByKey(Grouped.with(Serdes.Integer(), MySerdesFactory.employeeHistorySerde()))
                .aggregate(
                        // Initialized Aggregator
                        EmploymentHistoryAggregationDto::new,
                        //Aggregate
                        (empId, empHist, empHistAggr) -> {
                            empHistAggr.setEmpId(empId);
                            empHistAggr.add(empHist.getEmployerName());
                            return empHistAggr;
                        },
                        // store in materialied view EMPLOYMENT-HIST-AGGR-MV
                        Materialized.<Integer, EmploymentHistoryAggregationDto, KeyValueStore<Bytes, byte[]>>
                            as("EMPLOYMENT-HIST-AGGR-MV")
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(MySerdesFactory.employmentHistoryAggregationSerde())
                );

        // b. join with EMP-DEPT. Since the key is already identical, which is EMP_ID, no need FK Extractor
        final KTable<Integer, EmployeeResultDto> empResultTable =
            empDeptTable.join(employmentHistoryAggr,
                // Value Joiner
                (empResult, histAggr) -> {
                    empResult.setEmploymentHistory(histAggr.getEmploymentHistory());
                    return empResult;
                },
                // store in materialied view EMP-RESULT-MV
                Materialized.<Integer, EmployeeResultDto, KeyValueStore<Bytes, byte[]>>
                        as("EMP-RESULT-MV")
                        .withKeySerde(Serdes.Integer())
                        .withValueSerde(MySerdesFactory.employeeResultSerde())
            );

        // store result to output topic EMP-RESULT
        empResultTable.toStream()
                .map((key, value) -> new KeyValue<>(value.getEmpId(), value))
                .peek((key,value) -> System.out.println("(empResultTable) key,value = " + key  + "," + value))
                .to("EMP-RESULT", Produced.with(Serdes.Integer(), MySerdesFactory.employeeResultSerde()));
    }
}
