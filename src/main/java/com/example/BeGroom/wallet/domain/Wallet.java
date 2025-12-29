package com.example.BeGroom.wallet.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.member.domain.Member;
import jakarta.persistence.*;

@Entity
public class Wallet extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @JoinColumn(name = "member")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(nullable = false)
    private Long balance;

}
