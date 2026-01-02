package com.example.BeGroom.settlement.repository.daily;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySettlementRepositoryImpl implements DailySettlementRepositoryCustom {

    @Override
    public void upsert(LocalDate date, Long sellerId, Long paymentAmount,
                       BigDecimal feeRate, BigDecimal settlementAmount, BigDecimal refundAmount){

    }

    @Override
    public void updateRefund(BigDecimal refundAmount){

    }
}
