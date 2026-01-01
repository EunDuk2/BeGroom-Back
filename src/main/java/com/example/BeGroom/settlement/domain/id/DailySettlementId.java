package com.example.BeGroom.settlement.domain.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class DailySettlementId implements Serializable {
    // 정산일
    @Column(nullable = false)
    private LocalDate date;
    // 판매자ID
    @Column(nullable = false)
    private Long sellerId;
}
