package com.example.BeGroom.settlement.domain.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class YearlySettlementId implements Serializable {
    // 년
    @Column(nullable = false)
    private int year;
    // 판매자ID
    @Column(nullable = false)
    private Long sellerId;
}
