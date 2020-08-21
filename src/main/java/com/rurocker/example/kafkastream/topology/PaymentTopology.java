package com.rurocker.example.kafkastream.topology;

import com.rurocker.example.kafkastream.dto.PaymentAggregationDto;
import com.rurocker.example.kafkastream.dto.PaymentDto;
import com.rurocker.example.kafkastream.serde.MySerdesFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

public class PaymentTopology {

    public void createTopology(StreamsBuilder builder) {

        // Credit Card Stream
        builder.stream("CC-PAYMENT",
                    Consumed.with(Serdes.String(), MySerdesFactory.ccPaymentSerde()))
                .peek((key,value) -> System.out.println("(cc-payment) key,value = " + key  + "," + value))
                .mapValues( cc -> {
                    PaymentDto paymentDto = new PaymentDto();
                    paymentDto.setAccountName(cc.getCardHolder());
                    paymentDto.setAccountNo(maskNumber(cc.getCardNo()));
                    paymentDto.setBankName(cc.getIssuer());
                    paymentDto.setCurrency("IDR"); // assume all CC transactions are converted to IDR
                    paymentDto.setCustomerId(cc.getCustomerId());
                    paymentDto.setTrxAmount(cc.getTrxAmount());
                    paymentDto.setTrxId(cc.getTrxId());
                    paymentDto.setTrxDate(cc.getTrxDate());
                    paymentDto.setPaymentType("CC");
                    return paymentDto;
                })
                .to("PAYMENT-OUTPUT",
                        Produced.with(Serdes.String(), MySerdesFactory.paymentSerde()));

        // Bank Transfer Stream
        builder.stream("BANK-TRANSFER-PAYMENT",
                    Consumed.with(Serdes.String(), MySerdesFactory.bankTransferPaymentSerde()))
                .peek((key,value) -> System.out.println("(btrf-payment) key,value = " + key  + "," + value))
                .mapValues( bank -> {
                    PaymentDto paymentDto = new PaymentDto();
                    paymentDto.setAccountName(bank.getAccountName());
                    paymentDto.setAccountNo(bank.getAccountNo());
                    paymentDto.setBankName(bank.getBankName());
                    paymentDto.setCurrency(bank.getCurrency());
                    paymentDto.setCustomerId(bank.getCustomerId());
                    paymentDto.setTrxAmount(bank.getTrxAmount());
                    paymentDto.setTrxId(bank.getTrxId());
                    paymentDto.setTrxDate(bank.getTrxDate());
                    paymentDto.setPaymentType("BANK_TRF");
                    return paymentDto;
                })
                .to("PAYMENT-OUTPUT",
                        Produced.with(Serdes.String(), MySerdesFactory.paymentSerde()));

        // Get last 10 trx
        builder.stream("PAYMENT-OUTPUT",
                    Consumed.with(Serdes.String(), MySerdesFactory.paymentSerde()))
                .map((key, paymentDto) -> new KeyValue<>(paymentDto.getCustomerId(), paymentDto))
                .peek((key,value) -> System.out.println("(payment-output) key,value = " + key  + "," + value))
                .groupByKey(Grouped.with(Serdes.Integer(), MySerdesFactory.paymentSerde()))
                .aggregate(PaymentAggregationDto::new,
                        (custId, paymentDto, aggr) -> {
                            aggr.setCustomerId(custId);
                            aggr.add(paymentDto);
                            return aggr;
                        },
                        Materialized.<Integer, PaymentAggregationDto, KeyValueStore<Bytes, byte[]>>
                                as("PAYMENT-AGGR-MV")
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(MySerdesFactory.paymentAggregationSerde()))
                .toStream()
                .peek((key,value) -> System.out.println("(paymentAggr) key,value = " + key  + "," + value))
                .to("PAYMENT-TRX-AGGR",
                        Produced.with(Serdes.Integer(), MySerdesFactory.paymentAggregationSerde()));
    }

    private String maskNumber(String cardNum) {
        final int STARTLENGTH = 6;
        final int ENDLENGTH = 4;

        int maskedLength = cardNum.length() - (STARTLENGTH + ENDLENGTH);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maskedLength; i++) {
            sb.append("*");
        }
        return cardNum.substring(0, STARTLENGTH) + sb + cardNum.substring(cardNum.length() - ENDLENGTH);
    }
}
