package com.example.BeGroom.settlement.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.seller.domain.Seller;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private BigDecimal feeRate;
    // 수수료
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fee;
    // 정산금액
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal settlementAmount;
    // 정산상태
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementStatus status;
    // 결제상태
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    // 환불금액
    @Column(precision = 12, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;
    // 정산일
    @Column(nullable = false)
    private LocalDate payoutDate;
    // 집계 처리 여부 플래그
    @Column(nullable = false)
    private Boolean aggregated = false;

    public void markAggregated(){
        this.aggregated = true;
    }
}
