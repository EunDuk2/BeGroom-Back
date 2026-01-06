package com.example.BeGroom.settlement.scheduler;

import com.example.BeGroom.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final SettlementService settlementService;

    @Scheduled(fixedDelay = 10000)
    public void run() {
        settlementService.aggregateApprovedPayments();
        settlementService.syncRefundedPayments();
    }
}
