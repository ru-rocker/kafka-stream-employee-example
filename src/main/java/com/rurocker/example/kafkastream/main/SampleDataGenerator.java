package com.rurocker.example.kafkastream.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rurocker.example.kafkastream.dto.BankTransferPaymentDto;
import com.rurocker.example.kafkastream.dto.CCPaymentDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SampleDataGenerator {

    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        List<CCPaymentDto> list1 = new ArrayList<>();
        List<BankTransferPaymentDto> list2 = new ArrayList<>();

        for(int i = 0 ; i < 25 ; i++) {

            final int customerId = ThreadLocalRandom.current().nextInt(1, 3);

            final boolean flag = ThreadLocalRandom.current().nextBoolean();

            if(flag)
                list1.add(getCcPaymentDto(customerId));
            else
                list2.add(getBankTransferPaymentDto(customerId));

            Thread.sleep(50);
        }

        for (CCPaymentDto dto : list1)
            System.out.println(mapper.writeValueAsString(dto));

        for (BankTransferPaymentDto dto : list2)
            System.out.println(mapper.writeValueAsString(dto));

    }

    private static BankTransferPaymentDto getBankTransferPaymentDto(int customerId) {
        long smallest = 10_0000_0000L;
        long biggest =  99_9999_9999L;

        BankTransferPaymentDto dto = new BankTransferPaymentDto();
        dto.setAccountName(customerId == 1 ? "Fowler" : "Farah");
        dto.setAccountNo(String.valueOf(ThreadLocalRandom.current().nextLong(smallest, biggest+1)));
        dto.setBankName("ATOZ BANK");
        dto.setCurrency("IDR");
        dto.setCustomerId(customerId);
        dto.setReferenceNo(String.valueOf(ThreadLocalRandom.current().nextLong(smallest, biggest+1)));
        dto.setTrxAmount(new BigDecimal(ThreadLocalRandom.current().nextInt(100,500) * 1000));
        dto.setTrxDate(new Date());
        dto.setTrxId(System.currentTimeMillis());
        return dto;
    }

    private static CCPaymentDto getCcPaymentDto(int customerId) {

        long smallest = 1000_0000_0000_0000L;
        long biggest =  9999_9999_9999_9999L;

        CCPaymentDto dto1 = new CCPaymentDto();
        dto1.setCardHolder( customerId == 1 ? "Fowler" : "Farah");
        dto1.setCardNo(String.valueOf(ThreadLocalRandom.current().nextLong(smallest, biggest+1)));
        dto1.setCustomerId(customerId);

        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 3);

        dto1.setExpiryDate(cal.getTime());
        dto1.setIssuer("DUKE BANK");
        dto1.setTrxAmount(new BigDecimal(ThreadLocalRandom.current().nextInt(100,500) * 1000));
        dto1.setTrxDate(new Date());
        dto1.setTrxId(System.currentTimeMillis());
        return dto1;
    }
}
