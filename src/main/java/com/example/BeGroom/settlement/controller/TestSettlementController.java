package com.example.BeGroom.settlement.controller;

import com.example.BeGroom.settlement.service.SettlementService;
import com.example.BeGroom.settlement.service.aggregation.AggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/settlement")
@RequiredArgsConstructor
public class TestSettlementController {

    private final SettlementService settlementService;
    private final AggregationService aggregationService;

    // 1. 결제 완료 -> 정산 테이블 적재 테스트용
    @PostMapping("/aggregate-payments")
    public ResponseEntity<String> testAggregate(){
        settlementService.aggregateApprovedPayments();
        settlementService.syncRefundedPayments();
        return ResponseEntity.ok("승인 완료된 미정산 결제건 적재 스케줄러 테스트");
    }

    // 2. 정산 완료 -> 기간별(일/주/월/연) 집계 테스트용
    @PostMapping("/period-aggregation")
    public ResponseEntity<String> testPeriodAggregation() {
        aggregationService.aggregate();
        return ResponseEntity.ok("기간별 정산 집계 스케줄러 테스트");
    }

    // 3. 미정산건 지급 처리 테스트용
    @PostMapping("/payout")
    public ResponseEntity<String> testPayout() {
        settlementService.executeSettlementPayout();
        return ResponseEntity.ok("미정산 지급 처리 스케줄러 테스트");
    }

}
