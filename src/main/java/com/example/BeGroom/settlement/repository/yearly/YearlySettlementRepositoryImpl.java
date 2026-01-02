package com.example.BeGroom.settlement.repository.yearly;

import java.math.BigDecimal;
import java.time.LocalDate;

public class YearlySettlementRepositoryImpl implements YearlySettlementRepositoryCustom{

    @Override
    public void upsert(int year, Long sellerId, Long paymentAmount,
                BigDecimal feeRate, BigDecimal settlementAmount, BigDecimal refundAmount,
                LocalDate startDate, LocalDate endDate){

    }

    @Override
    public void updateRefund(BigDecimal returnAmount){

    }
}
