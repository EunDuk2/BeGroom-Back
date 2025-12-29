package com.example.BeGroom.wallet.domain;

import com.example.BeGroom.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
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

}
