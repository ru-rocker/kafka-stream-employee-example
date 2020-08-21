package com.rurocker.example.kafkastream.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaymentAggregationDto {

    private Integer customerId;
    private List<PaymentDto> payments = new ArrayList<>(10);

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public List<PaymentDto> getPayments() {
        return payments;
    }

    public void add(PaymentDto dto) {
        if(payments.contains(dto)) {
            return;
        }
        payments.add(dto);

        Collections.sort(this.payments, (o1, o2) -> o2.getTrxDate().compareTo(o1.getTrxDate()));
        if(payments.size() > 10) {
            payments.remove(10);
        }
    }
}
