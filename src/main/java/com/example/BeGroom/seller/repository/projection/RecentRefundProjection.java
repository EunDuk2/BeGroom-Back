package com.example.BeGroom.seller.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// PaymentRepository에서 사용
public interface RecentRefundProjection {
    Long getPaymentId();
    BigDecimal getRefundAmount();
    LocalDateTime getCreatedAt();
}
