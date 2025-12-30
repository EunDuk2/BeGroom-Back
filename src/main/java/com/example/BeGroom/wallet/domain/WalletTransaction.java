package com.example.BeGroom.wallet.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTransaction extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @JoinColumn(name = "wallet_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Wallet wallet;

    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private TransactionType transactionType;

    @Column(nullable = false)
    private Long balanceBefore;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long balanceAfter;

    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private ReferenceType referenceType;

    @Column(nullable = false)
    private Long referenceId;

    private WalletTransaction(Wallet wallet, TransactionType transactionType, Long balanceBefore, Long amount, Long balanceAfter, ReferenceType referenceType, Long referenceId) {
        this.wallet = wallet;
        this.transactionType = transactionType;
        this.balanceBefore = balanceBefore;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    public static WalletTransaction create(Wallet wallet, TransactionType transactionType, Long balanceBefore, Long amount, Long balanceAfter, ReferenceType referenceType, Long referenceId) {
         return new WalletTransaction(
                 wallet, transactionType, balanceBefore, amount, balanceAfter, referenceType, referenceId);
    }

}
