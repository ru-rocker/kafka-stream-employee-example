package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PaymentDto {

    private Long trxId;
    private BigDecimal trxAmount;
    private Integer customerId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date trxDate;

    // CC or BANK_TRF
    private String paymentType;

    private String accountNo;
    private String accountName;
    private String bankName; // or issuer
    private String referenceNo;
    private String currency;

    // and the rest of payment information
}
