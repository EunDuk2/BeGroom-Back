package com.example.BeGroom.settlement.repository.monthly;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthlySettlementRepositoryCustom {

    void upsert(int year, int month, Long sellerId, Long paymentAmount,
                BigDecimal feeRate, BigDecimal settlementAmount, BigDecimal refundAmount,
                LocalDate startDate, LocalDate endDate);

    void updateRefund(BigDecimal returnAmount);
}
