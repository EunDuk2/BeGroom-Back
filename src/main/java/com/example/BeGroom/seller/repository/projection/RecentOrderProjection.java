package com.example.BeGroom.seller.repository.projection;

import java.time.LocalDateTime;

// orderRepository 최근 주문 조회에 필요
public interface RecentOrderProjection {
    Long getOrderId();
    Long getTotalAmount();
    LocalDateTime getApprovedAt();
}
