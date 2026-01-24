package com.example.BeGroom.settlement.scheduler;

import com.example.BeGroom.settlement.service.SettlementService;
import com.example.BeGroom.settlement.service.aggregation.AggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final SettlementService settlementService;
    private final AggregationService aggregationService;

    @Scheduled(fixedDelay = 10000)
    public void run() {
        aggregateSettlement();
    }

    // 승인 완료된 미정산 결제건 정산 테이블에 적재
    private void aggregateSettlement(){
        settlementService.aggregateApprovedPayments();
        settlementService.syncRefundedPayments();
    }

    // 미정산건 지급
    private void payoutSettlement(){
        settlementService.executeSettlementPayout();
    }

    // 기간별 정산 집계
    private void periodAggregation(){
        aggregationService.aggregate();
    }
}
