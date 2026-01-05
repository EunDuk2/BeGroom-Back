package com.example.BeGroom.settlement.repository.weekly;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
public class WeeklySettlementRepositoryImpl implements WeeklySettlementRepositoryCustom{

    private final EntityManager em;

    @Override
    public void upsertAggregate(int year, int month, int week, Long sellerId, Long paymentAmount,
                       BigDecimal fee, BigDecimal settlementAmount, BigDecimal refundAmount,
                       LocalDate startDate, LocalDate endDate){

        String sql = "insert into weekly_settlement(year, month, week, seller_id, payment_amount, refund_amount, fee, settlement_amount, start_date, end_date)" +
                "values(:year, :month, :week, :sellerId, :paymentAmount, :refundAmount, :fee, (:paymentAmount - :fee), :startDate, :endDate) " +
                "on duplicate key " +
                "update payment_amount = payment_amount + :paymentAmount," +
                "fee = fee + :fee," +
                "settlement_amount = (payment_amount - fee - refund_amount)";

        em.createNativeQuery(sql)
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("week", week)
                .setParameter("sellerId", sellerId)
                .setParameter("paymentAmount", paymentAmount)
                .setParameter("fee", fee)
                .setParameter("refundAmount", refundAmount)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .executeUpdate();

    }

    @Override
    public void updateRefund(int year, int month, int week, Long sellerId, BigDecimal fee, BigDecimal refundAmount){

        String sql = "update weekly_settlement " +
                "set refund_amount = refund_amount + :refundAmount, " +
                "fee = fee - :fee, " +
                "settlement_amount = (payment_amount - fee - refund_amount) " +
                "where year = :year " +
                "and month = :month " +
                "and week = :week " +
                "and seller_id = :sellerId";

        em.createNativeQuery(sql)
                .setParameter("year", year)
                .setParameter("month", month)
                .setParameter("week", week)
                .setParameter("sellerId", sellerId)
                .setParameter("fee", fee)
                .setParameter("refundAmount", refundAmount)
                .executeUpdate();
    }

}
