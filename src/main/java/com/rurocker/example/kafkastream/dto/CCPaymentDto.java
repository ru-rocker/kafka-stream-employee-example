package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CCPaymentDto {

    private Long trxId;
    private BigDecimal trxAmount;
    private String cardNo;
    private String cardHolder;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expiryDate;
    private String issuer;
    private Integer customerId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date trxDate;
}
