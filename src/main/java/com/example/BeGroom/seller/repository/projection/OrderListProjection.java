package com.example.BeGroom.seller.repository.projection;

import java.time.LocalDateTime;

// orderRepository 판매자 주문 목록 조회에 필요
public interface OrderListProjection {

    Long getOrderId();
    LocalDateTime getCreatedAt();
    Long getTotalAmount();
    String getPaymentMethod();
    String getSettlementStatus();
    String getPaymentStatus();

}
