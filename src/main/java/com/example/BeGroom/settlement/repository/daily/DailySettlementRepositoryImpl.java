package com.example.BeGroom.settlement.repository.daily;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
public class DailySettlementRepositoryImpl implements DailySettlementRepositoryCustom {

    private final EntityManager em;

    @Override
    public void upsertAggregate(LocalDate date, Long sellerId, Long paymentAmount,
                       BigDecimal fee, BigDecimal settlementAmount, BigDecimal refundAmount){

        String sql = "insert into daily_settlement(date, seller_id, payment_amount, refund_amount, fee, settlement_amount)" +
                "values(:date, :sellerId, :paymentAmount, :refundAmount, :fee, (:paymentAmount - :fee))" +
                "on duplicate key " +
                "update payment_amount = payment_amount + :paymentAmount," +
                "fee = fee + :fee," +
                "settlement_amount = (payment_amount - fee - refund_amount)";

        em.createNativeQuery(sql)
                .setParameter("date", date)
                .setParameter("sellerId", sellerId)
                .setParameter("paymentAmount", paymentAmount)
                .setParameter("fee", fee)
                .setParameter("refundAmount", refundAmount)
                .executeUpdate();
    }

    @Override
    public void updateRefund(LocalDate date, Long sellerId, BigDecimal fee, BigDecimal refundAmount){

        String sql = "update daily_settlement " +
                "set refund_amount = refund_amount + :refundAmount, " +
                "fee = fee - :fee, " +
                "settlement_amount = (payment_amount - fee - refund_amount) " +
                "where date = :date " +
                "and seller_id = :sellerId";

         em.createNativeQuery(sql)
                 .setParameter("date", date)
                 .setParameter("sellerId", sellerId)
                 .setParameter("fee", fee)
                 .setParameter("refundAmount", refundAmount)
                 .executeUpdate();
    }
}
