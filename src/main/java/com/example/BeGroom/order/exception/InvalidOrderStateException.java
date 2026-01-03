package com.example.BeGroom.order.exception;

import com.example.BeGroom.order.domain.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {

    private final OrderStatus currentStatus;

    public InvalidOrderStateException(String action, OrderStatus currentStatus) {
        super("주문 상태 [" + currentStatus + "]에서는 [" + action + "] 할 수 없습니다.");
        this.currentStatus = currentStatus;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }
}
