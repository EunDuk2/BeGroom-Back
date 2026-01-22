package com.example.BeGroom.settlement.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.seller.domain.Seller;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "settlement",
        indexes = {
                @Index(name = "idx_settlement_seller", columnList = "seller_id"),
                @Index(name = "idx_settlement_payment", columnList = "payment_id")
        }
)
public class Settlement extends BaseEntity {

    // 정산ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 판매자ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;
    // 결제ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
    // 결제금액
    @Column(nullable = false)
    private Long paymentAmount;
    // 수수료율
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate = BigDecimal.valueOf(10.00);
    // 수수료
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fee;
    // 정산금액
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal settlementAmount;
    // 정산상태
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementStatus status = SettlementStatus.UNSETTLED;
    // 결제상태
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementPaymentStatus settlementPaymentStatus = SettlementPaymentStatus.PAYMENT;
    // 환불금액
    @Column(precision = 12, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;
    // 지급일
    @Column()
    private LocalDateTime payoutDate;
    // 정산일
//    @Column()
//    private LocalDate date = LocalDate.now();
    // 집계 여부
    @Column(nullable = false)
    private Boolean isAggregated;

    public void markAggregated(){
//        this.status = SettlementStatus.SETTLED;
        this.isAggregated = true;
    }

    public void markRefunded(BigDecimal refundAmount) {
        this.settlementPaymentStatus = SettlementPaymentStatus.REFUND;
        this.refundAmount = refundAmount;
    }

    // 미정산 지급 실행
    public void markSettled() {
        this.status = SettlementStatus.SETTLED;
        this.payoutDate = LocalDateTime.now();
    }

    @Builder
    private Settlement(
            Seller seller,
            Payment payment,
            Long paymentAmount,
            BigDecimal feeRate,
            BigDecimal fee,
            BigDecimal settlementAmount,
            SettlementStatus status,
            SettlementPaymentStatus settlementPaymentStatus,
            BigDecimal refundAmount
    ) {
        this.seller = seller;
        this.payment = payment;
        this.paymentAmount = paymentAmount;
        this.feeRate = feeRate;
        this.fee = fee;
        this.settlementAmount = settlementAmount;
        this.status = status;
        this.settlementPaymentStatus = settlementPaymentStatus;
        this.refundAmount = refundAmount != null ? refundAmount : BigDecimal.ZERO;
        this.payoutDate = payoutDate;
    }


    public static Settlement create(Payment payment) {
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
                .settlementPaymentStatus(SettlementPaymentStatus.PAYMENT)
                .refundAmount(BigDecimal.ZERO)
                .build();
    }


}
