package com.example.BeGroom.settlement.domain.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class YearlySettlementId implements Serializable {
    // 년
    @Column(name = "settlement_year", nullable = false)
    private int year;
    // 판매자ID
    @Column(nullable = false)
    private Long sellerId;

    @Builder
    public YearlySettlementId(int year, Long sellerId){
        this.year = year;
        this.sellerId = sellerId;
    }
}
