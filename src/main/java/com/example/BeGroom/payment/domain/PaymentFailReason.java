package com.example.BeGroom.payment.domain;

public enum PaymentFailReason {
    INSUFFICIENT_BALANCE,
    INSUFFICIENT_STOCK,
    PG_REJECTED,
    TIMEOUT,
    SYSTEM_ERROR
}
