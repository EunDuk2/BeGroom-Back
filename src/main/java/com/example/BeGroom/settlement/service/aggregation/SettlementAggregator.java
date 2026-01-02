package com.example.BeGroom.settlement.service.aggregation;

import com.example.BeGroom.settlement.domain.Settlement;

public interface SettlementAggregator {
    void aggregate(Settlement settlement);
    void refund(Settlement settlement);
}
