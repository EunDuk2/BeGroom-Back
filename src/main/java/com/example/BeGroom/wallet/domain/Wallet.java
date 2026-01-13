package com.example.BeGroom.wallet.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.wallet.exception.InsufficientBalanceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(nullable = false)
    private Long balance;

    private Wallet(Member member, Long balance) {
        this.member = member;
        this.balance = balance;
    }

    public static Wallet create(Member member) {
        return new Wallet(member, 0L);
    }

    public void increaseBalance(long amount) {
        this.balance += amount;
    }

    public void decreaseBalance(long amount) {
        if(this.balance < amount) {
            throw new InsufficientBalanceException(this.id);
        }
        this.balance -= amount;
    }

    public boolean canPay(Long payAmount) {
        return this.balance > payAmount;
    }

    public void pay(Long payAmount) {
        if(this.balance < payAmount) {
            throw new InsufficientBalanceException(this.id);
        }
        this.balance -= payAmount;
    }


}
