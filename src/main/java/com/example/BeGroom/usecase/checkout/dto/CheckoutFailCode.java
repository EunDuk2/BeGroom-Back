package com.example.BeGroom.usecase.checkout.dto;

public enum CheckoutFailCode {
    INSUFFICIENT_BALANCE("포인트 잔액이 부족합니다."),
    INSUFFICIENT_STOCK("상품 재고가 부족합니다.");

    private final String message;

    CheckoutFailCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

