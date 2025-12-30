package com.example.BeGroom.wallet.domain;

public enum TransactionType {
    CHARGE("충전"),
    PAYMENT("결제"),
    REFUND("환불");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

