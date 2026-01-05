package com.example.BeGroom.seller.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RecentSettlementProjection {
    Long getSettlementId();
    BigDecimal getSettlementAmount();
    LocalDateTime getCreatedAt();
}
