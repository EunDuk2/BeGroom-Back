package com.example.BeGroom.settlement.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.settlement.domain.id.DailySettlementId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailySettlement extends BaseEntity {
    // 복합키(정산일+판매자ID)
    @EmbeddedId
    private DailySettlementId id;
    // 결제금액
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal paymentAmount;
    // 수수료
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;
    // 정산금액
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal settlementAmount;
    // 환불금액
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

//    // 환불 재집계 (update)
//    public void updateRefund(BigDecimal paymentAmount, BigDecimal feeRate,
//                             BigDecimal settlementAmount, BigDecimal refundAmount){
//        this.paymentAmount = paymentAmount;
//        this.fee = fee;
//        this.settlementAmount = settlementAmount;
//        this.refundAmount = refundAmount;
//    }

    @Builder
    public DailySettlement(DailySettlementId id, BigDecimal paymentAmount, BigDecimal fee,
                           BigDecimal settlementAmount, BigDecimal refundAmount) {
        this.id = id;
        this.paymentAmount = paymentAmount;
        this.fee = fee;
        this.settlementAmount = settlementAmount;
        this.refundAmount = refundAmount;
    }
}
