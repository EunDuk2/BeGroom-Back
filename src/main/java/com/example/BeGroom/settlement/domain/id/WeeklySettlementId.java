package com.example.BeGroom.settlement.domain.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class WeeklySettlementId implements Serializable {
    // 년
    @Column(nullable = false)
    private int year;
    // 월
    @Column(nullable = false)
    private int month;
    // 주차
    @Column(nullable = false)
    private int week;
    // 판매자ID
    @Column(nullable = false)
    private Long sellerId;

    @Builder
    public WeeklySettlementId(int year, int month, int week, Long sellerId){
        this.year = year;
        this.month = month;
        this.week = week;
        this.sellerId = sellerId;
    }
}
