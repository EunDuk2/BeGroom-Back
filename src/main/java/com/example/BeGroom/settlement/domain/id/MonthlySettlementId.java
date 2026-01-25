package com.example.BeGroom.settlement.domain.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class MonthlySettlementId implements Serializable {
    // 년
    @Column( nullable = false)
    private int year;
    // 월
    @Column( nullable = false)
    private int month;
    // 판매자ID
    @Column(nullable = false)
    private Long sellerId;

    @Builder
    public MonthlySettlementId(int year, int month, Long sellerId){
        this.year = year;
        this.month = month;
        this.sellerId = sellerId;
    }
}
