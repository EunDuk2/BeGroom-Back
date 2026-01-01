package com.example.BeGroom.settlement.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.settlement.domain.id.YearlySettlementId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class YearlySettlement extends BaseEntity {

    // 복합키(년+판매자ID)
    @EmbeddedId
    private YearlySettlementId id;
    // 결제금액
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal paymentAmount;
    // 수수료율
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate = BigDecimal.ZERO;
    // 정산금액
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal settlementAmount;
    // 환불금액
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;
    // 시작일
    @Column(nullable = false)
    private LocalDate startDate;
    // 종료일
    @Column(nullable = false)
    private LocalDate endDate;

    @Builder
    public YearlySettlement(int year, Long sellerId,
                            BigDecimal paymentAmount, BigDecimal feeRate, BigDecimal settlementAmount,
                            BigDecimal refundAmount, LocalDate startDate, LocalDate endDate){
        this.id = new YearlySettlementId(year, sellerId);
        this.paymentAmount = paymentAmount;
        this.feeRate = feeRate;
        this.settlementAmount = settlementAmount;
        this.refundAmount = refundAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
