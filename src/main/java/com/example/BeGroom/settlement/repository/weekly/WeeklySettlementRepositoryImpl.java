package com.example.BeGroom.settlement.repository.weekly;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class WeeklySettlementRepositoryImpl implements WeeklySettlementRepositoryCustom{

    @Override
    public void upsert(int year, int month, int week, Long sellerId, Long paymentAmount,
                       BigDecimal feeRate, BigDecimal settlementAmount, BigDecimal refundAmount,
                       LocalDate startDate, LocalDate endDate){

    }

    @Override
    public void updateRefund(BigDecimal refundAmount){

    }

}
