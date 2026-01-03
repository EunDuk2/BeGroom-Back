package com.example.BeGroom.payment.exception;


import com.example.BeGroom.payment.domain.PaymentStatus;

public class InvalidPaymentStateException extends RuntimeException {

    private final PaymentStatus currentStatus;

    public InvalidPaymentStateException(String action, PaymentStatus currentStatus) {
        super("결제 상태 [" + currentStatus + "]에서는 [" + action + "] 할 수 없습니다.");
        this.currentStatus = currentStatus;
    }

    public PaymentStatus getCurrentStatus() {
        return currentStatus;
    }
}
