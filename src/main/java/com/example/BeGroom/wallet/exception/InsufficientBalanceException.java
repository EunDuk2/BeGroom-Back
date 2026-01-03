package com.example.BeGroom.wallet.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(Long walletId) {
        super("포인트 잔액이 부족합니다. walletId=" + walletId);
    }
}

