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

import static com.example.BeGroom.wallet.domain.ReferenceType.ORDER;
import static com.example.BeGroom.wallet.domain.TransactionType.PAYMENT;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalletTransaction> transactions = new ArrayList<>();

    @Column(nullable = false)
    private Long balance;

    private Wallet(Member member, Long balance) {
        this.member = member;
        this.balance = balance;
    }

    public static Wallet create(Member member, long balance) {
        return new Wallet(member, balance);
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
        return this.balance >= payAmount;
    }

    public void pay(Long payAmount, ReferenceType referenceType, Long referenceId) {
        long before = balance;

        if(before < payAmount) {
            throw new InsufficientBalanceException(this.id);
        }
        this.balance -= payAmount;

        recordTransaction(
                PAYMENT,
                before,
                payAmount,
                this.balance,
                referenceType,
                referenceId
        );
    }

    private void recordTransaction(
            TransactionType transactionType,
            long before,
            long changeAmount,
            long after,
            ReferenceType referenceType,
            long referenceId
    ) {
        WalletTransaction tx = WalletTransaction.create(
                this, transactionType, before, -changeAmount, after, referenceType, referenceId
        );
        transactions.add(tx);
    }


}
