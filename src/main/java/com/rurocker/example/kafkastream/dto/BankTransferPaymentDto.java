package com.rurocker.example.kafkastream.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BankTransferPaymentDto {

    private Long trxId;
    private BigDecimal trxAmount;
    private String accountNo;
    private String accountName;
    private String bankName;
    private String referenceNo;
    private String currency;
    private Integer customerId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private Date trxDate;

}
