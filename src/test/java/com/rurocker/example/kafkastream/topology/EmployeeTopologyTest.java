package com.rurocker.example.kafkastream.topology;

import com.rurocker.example.kafkastream.dto.DepartmentDto;
import com.rurocker.example.kafkastream.dto.EmployeeDto;
import com.rurocker.example.kafkastream.dto.EmployeeResultDto;
import com.rurocker.example.kafkastream.dto.EmploymentHistoryDto;
import com.rurocker.example.kafkastream.serde.MySerdesFactory;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ricky Martaputra
 * Created on 06-Dec-2020 4:40 PM
 */
public class EmployeeTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<Integer, DepartmentDto> deptInput;
    private TestInputTopic<Integer, EmployeeDto> employeeInput;
    private TestInputTopic<Integer, EmploymentHistoryDto> employmentHistoryInput;
    private TestOutputTopic<Integer, EmployeeResultDto> employeeOutput;
    private final Serde<Integer> integerSerde = new Serdes.IntegerSerde();


    @BeforeEach
    public void init() {

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "app-id");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, integerSerde.getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, MySerdesFactory.employeeSerde().getClass());

        EmployeeTopology employeeTopology = new EmployeeTopology();
        final StreamsBuilder builder = new StreamsBuilder();
        employeeTopology.createTopology(builder);
        final Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);
        deptInput = testDriver.createInputTopic("DEPT", integerSerde.serializer(),
                MySerdesFactory.departmentSerde().serializer());
        employeeInput = testDriver.createInputTopic("EMPLOYEE", integerSerde.serializer(),
                MySerdesFactory.employeeSerde().serializer());
        employmentHistoryInput = testDriver.createInputTopic("EMPLOYMENT-HISTORY", integerSerde.serializer(),
                MySerdesFactory.employeeHistorySerde().serializer());
        employeeOutput = testDriver.createOutputTopic("EMP-RESULT", integerSerde.deserializer(),
                MySerdesFactory.employeeResultSerde().deserializer());
    }

    @AfterEach
    public void tearDown() throws IOException {
        testDriver.close();
        FileUtils.deleteDirectory(new File("/tmp/kafka-streams/app-id"));
    }

    @Test
    @DisplayName("Test Employee Topology between department and employee, exclude employment history")
    public void testEmployeeAggregationTopology() {

        // Finance Department
        DepartmentDto financeDept = DepartmentDto.builder()
                .deptId(1)
                .deptName("Finance")
                .build();

        // Employee: Alice
        EmployeeDto alice = EmployeeDto.builder()
                .empId(1000)
                .empName("Alice")
                .deptId(1)
                .build();

        // expeceted output
        EmployeeResultDto expected = new EmployeeResultDto();
        expected.setDeptId(1);
        expected.setDeptName("Finance");
        expected.setEmpId(1000);
        expected.setEmpName("Alice");

        // 1.1 insert finance dept to DEPT topic.
        // Remember: I put key as null value because we do key repartitioning in deptTable.
        // But this depends on your use case.
        deptInput.pipeInput(null, financeDept);
        // 1.2 output topic (EMP-RESULT) is empty because inner join behaviour between employee and dept
        assertThat(employeeOutput.isEmpty()).isTrue();

        // 2.1 insert employee to EMPLOYEE topic.
        // Remember: I put key as null value because we do key repartitioning in empTable.
        // But this depends on your use case.
        employeeInput.pipeInput(null, alice);
        // 2.2 output topic (EMP-RESULT) now is not empty because there are two stream data with associated key (dept_id)
        assertThat(employeeOutput.isEmpty()).isFalse();
        assertThat(employeeOutput.readKeyValue()).isEqualTo(new KeyValue<>(1000, expected));
        // 2.3 make sure no record left in the output topic
        assertThat(employeeOutput.isEmpty()).isTrue();

    }


    @Test
    @DisplayName("Test Employee Topology between department and employee, include employment history")
    public void testEmployeeAggregationTopologyWithEmployementHistory() {

        // Finance Department
        DepartmentDto financeDept = DepartmentDto.builder()
                .deptId(1)
                .deptName("Finance")
                .build();

        // Employee: Alice
        EmployeeDto alice = EmployeeDto.builder()
                .empId(1000)
                .empName("Alice")
                .deptId(1)
                .build();

        // History: Company A
        EmploymentHistoryDto historyCompanyA = EmploymentHistoryDto.builder()
                .empHistId(1)
                .empId(1000)
                .employerName("Company A")
                .build();

        // History: Company B
        EmploymentHistoryDto historyCompanyB = EmploymentHistoryDto.builder()
                .empHistId(1)
                .empId(1000)
                .employerName("Company B")
                .build();

        // expeceted output
        EmployeeResultDto expected = new EmployeeResultDto();
        expected.setDeptId(1);
        expected.setDeptName("Finance");
        expected.setEmpId(1000);
        expected.setEmpName("Alice");
        expected.setEmploymentHistory(Set.of("Company A", "Company B"));

        // 1. insert finance dept to DEPT topic.
        // Remember: I put key as null value because we do key repartitioning in deptTable.
        // But this depends on your use case.
        deptInput.pipeInput(null, financeDept);

        // 2. insert employee to EMPLOYEE topic.
        // Remember: I put key as null value because we do key repartitioning in empTable.
        // But this depends on your use case.
        employeeInput.pipeInput(null, alice);

        // 3. insert employee to EMPLOYMENT-HISTORY topic.
        // Remember: I put key as null value because we do key repartitioning in empTable.
        // But this depends on your use case.
        employmentHistoryInput.pipeInput(null, historyCompanyA);
        employmentHistoryInput.pipeInput(null, historyCompanyB);

        // make sure topic is not empty
        assertThat(employeeOutput.isEmpty()).isFalse();
        // loop until last records, because we cannot predict the left-join behaviour.
        // what we now is only the last record should be as what we expected.
        KeyValue<Integer, EmployeeResultDto> kv = null;
        while(!employeeOutput.isEmpty()) {
            kv = employeeOutput.readKeyValue();
        }
        // make sure kv is not null
        assertThat(kv).isNotNull();
        assertThat(kv).isEqualTo(new KeyValue<>(1000, expected));
        // make sure no record left in the output topic
        assertThat(employeeOutput.isEmpty()).isTrue();
    }
}
