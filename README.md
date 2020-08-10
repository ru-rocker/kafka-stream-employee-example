# Overview
Example of joining Kafka Stream with 1:N and N:1 use case.

# Maven Dependencies
### Kafka Stream 

        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-streams</artifactId>
            <version>2.5.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>2.5.0</version>
        </dependency>


### Jackson

        <dependency>
            <groupId>com.fasterxml.jackson.module</groupId>
            <artifactId>jackson-module-parameter-names</artifactId>
            <version>2.11.0</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jdk8</artifactId>
            <version>2.11.0</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.11.0</version>
        </dependency>

### Lombok


        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.12</version>
            <scope>provided</scope>
        </dependency>
        

# Create Kafka Topic
Since this is a local setup, the partition and replication factor is set into 1.
So no issue about co-partition.

_Remember, for topics to be enable to join, data must  be co-partition._


        kafka-topics --zookeeper localhost:2181 --topic DEPT --create --replication-factor 1 --partitions 1
        kafka-topics --zookeeper localhost:2181 --topic EMPLOYEE --create --replication-factor 1 --partitions 1
        kafka-topics --zookeeper localhost:2181 --topic EMPLOYMENT-HISTORY --create --replication-factor 1 --partitions 1
        kafka-topics --zookeeper localhost:2181 --topic EMP-RESULT --create --replication-factor 1 --partitions 1
        

# Coding steps
1. DTO

         com.rurocker.example.kafkastream.dto
         
         
2. Create JSON Serde 
     
        com.rurocker.example.kafkastream.serde


3. Create Topology
     
        com.rurocker.example.kafkastream.topoplogy

   Select key first.
     
# Sample Payload
All the sample payloads are located under `test/resources/sample-data` folder.

# Expected Output
The output will be a 'complete' employee information with department name and employment history under one record.

      {
          "emp_id": 3,
          "dept_id": 2,
          "emp_name": "Charlie",
          "dept_name": "IT",
          "employment_history": [
              "ABC",
              "DEF"
          ]
      }