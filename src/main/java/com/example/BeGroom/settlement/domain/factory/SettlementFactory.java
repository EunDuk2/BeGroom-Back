package com.example.BeGroom.settlement.domain.factory;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.SettlementPaymentStatus;
import com.example.BeGroom.settlement.domain.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class SettlementFactory {

    public static Settlement create(Payment payment){

        Long paymentAmount = payment.getAmount();
        BigDecimal feeRate = new BigDecimal("10.00");
        BigDecimal fee = BigDecimal.valueOf(paymentAmount)
                .multiply(feeRate)
                .divide(new BigDecimal("100"));
        BigDecimal settlementAmount = BigDecimal.valueOf(paymentAmount).subtract(fee);

        Optional<Seller> sellerOpt = payment.getOrder()
                .getOrderProductList()
                .stream()
                .findFirst()
                .map(op -> op.getProductDetail().getProduct().getBrand().getSeller());

        Seller seller = sellerOpt.orElse(null);

        return Settlement.builder()
                .seller(seller)
                .payment(payment)
                .paymentAmount(paymentAmount)
                .fee(fee)
                .feeRate(feeRate)
                .settlementAmount(settlementAmount)
                .status(SettlementStatus.UNSETTLED)
                .paymentStatus(SettlementPaymentStatus.PAYMENT)
                .refundAmount(BigDecimal.ZERO)
                .date(LocalDate.now())
                .build();
    }
}
