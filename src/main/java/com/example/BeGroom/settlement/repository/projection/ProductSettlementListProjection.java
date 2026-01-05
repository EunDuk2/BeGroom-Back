package com.example.BeGroom.settlement.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ProductSettlementListProjection {

    Long getSettlementId();
    LocalDateTime getCreatedAt();
    Long getPaymentAmount();
    BigDecimal getRefundAmount();
    BigDecimal getFeeAmount();
    BigDecimal getSettlementAmount();
    String getSettlementStatus();
}
