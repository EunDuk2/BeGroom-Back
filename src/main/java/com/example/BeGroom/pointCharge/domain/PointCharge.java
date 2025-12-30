package com.example.BeGroom.pointCharge.domain;

import com.example.BeGroom.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointCharge {

    @Id @GeneratedValue
    private Long id;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private ChargeStatus chargeStatus;

    private PointCharge(Member member, Long amount, ChargeStatus chargeStatus) {
        this.member = member;
        this.amount = amount;
        this.chargeStatus = chargeStatus;
    }

    public static PointCharge create(Member member, Long amount, ChargeStatus chargeStatus) {
        return new PointCharge(member, amount, chargeStatus);
    }

    public void completeCharge() {
        this.chargeStatus = ChargeStatus.COMPLETED;
    }

}
