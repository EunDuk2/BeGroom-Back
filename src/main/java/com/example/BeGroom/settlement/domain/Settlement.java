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
    private PaymentStatus paymentStatus = PaymentStatus.PAYMENT;
    // 환불금액
    @Column(precision = 12, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;
    // 지급일
    @Column()
    private LocalDate payoutDate;
    // 정산일
    @Column()
    private LocalDate date = LocalDate.now();

    public void markAggregated(){
        this.status = SettlementStatus.SETTLED;
    }

    public void markRefunded(BigDecimal refundAmount) {
        this.paymentStatus = PaymentStatus.REFUND;
        this.refundAmount = refundAmount;
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
            PaymentStatus paymentStatus,
            BigDecimal refundAmount,
            LocalDate payoutDate,
            LocalDate date
    ) {
        this.seller = seller;
        this.payment = payment;
        this.paymentAmount = paymentAmount;
        this.feeRate = feeRate;
        this.fee = fee;
        this.settlementAmount = settlementAmount;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.refundAmount = refundAmount != null ? refundAmount : BigDecimal.ZERO;
        this.payoutDate = payoutDate;
        this.date = date;
    }
}
