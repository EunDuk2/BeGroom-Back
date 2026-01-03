package com.example.BeGroom.usecase.checkout.exception;

import com.example.BeGroom.usecase.checkout.dto.CheckoutFailCode;

public class CheckoutInsufficientBalanceException extends CheckoutException {

    public CheckoutInsufficientBalanceException(Long orderId, Long paymentId) {
        super(
                orderId,
                paymentId,
                CheckoutFailCode.INSUFFICIENT_BALANCE,
                "포인트 잔액이 부족합니다."
        );
    }

}
