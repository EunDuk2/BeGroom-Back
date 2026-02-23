package com.example.BeGroom.settlement.controller;

import com.example.BeGroom.settlement.service.SchedulerService;
import com.example.BeGroom.settlement.service.SettlementService;
import com.example.BeGroom.settlement.service.aggregation.AggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test/settlement")
@RequiredArgsConstructor
public class TestSettlementController {

    private final SettlementService settlementService;
    private final AggregationService aggregationService;
    private final SchedulerService schedulerService;

    // 1. 결제 완료 -> 정산 테이블 적재 테스트용
    @PostMapping("/aggregate-payments")
    public ResponseEntity<String> testAggregate(){

        StopWatch stopWatch = new StopWatch("Settlement Process");
        stopWatch.start("Batch Total Execution-1");

        try {
            schedulerService.aggregateApprovedPayments();

        } finally {
            stopWatch.stop();
            log.warn("##################################################");
            log.warn("전체 처리 시간: {} sec", stopWatch.getTotalTimeSeconds());
            log.warn("상세 리포트: \n{}", stopWatch.prettyPrint());
            log.warn("##################################################");
        }

        return ResponseEntity.ok("승인 완료된 미정산 결제건 적재 스케줄러 테스트");
    }

    // 2. 정산 테이블 적재 후 환불 건 환불 금액 업뎃 테스트용
    @PostMapping("/update-refund-payments")
    public ResponseEntity<String> testRefund(){

        StopWatch stopWatch = new StopWatch("Settlement Process");
        stopWatch.start("Batch Total Execution-2");

        try {
            schedulerService.syncRefundedPayments();

        } finally {
            stopWatch.stop();
            log.warn("##################################################");
            log.warn("전체 처리 시간: {} sec", stopWatch.getTotalTimeSeconds());
            log.warn("상세 리포트: \n{}", stopWatch.prettyPrint());
            log.warn("##################################################");
        }

        return ResponseEntity.ok("적재 후 환불 건 환불금액 반영 스케줄러 테스트");
    }

    // 3. 정산 완료 -> 기간별(일/주/월/연) 집계 테스트용
    @PostMapping("/period-aggregation")
    public ResponseEntity<String> testPeriodAggregation() {

        StopWatch stopWatch = new StopWatch("Settlement Process");
        stopWatch.start("Batch Total Execution-3");

        try {
            aggregationService.aggregate();

        } finally {
            stopWatch.stop();
            log.warn("##################################################");
            log.warn("전체 처리 시간: {} sec", stopWatch.getTotalTimeSeconds());
            log.warn("상세 리포트: \n{}", stopWatch.prettyPrint());
            log.warn("##################################################");
        }

        return ResponseEntity.ok("기간별 정산 집계 스케줄러 테스트");
    }

    // 4. 미정산건 지급 처리 테스트용
    @PostMapping("/payout")
    public ResponseEntity<String> testPayout() {

        StopWatch stopWatch = new StopWatch("Settlement Process");
        stopWatch.start("Batch Total Execution-4");

        try {
            schedulerService.executeSettlementPayout();

        } finally {
            stopWatch.stop();
            log.warn("##################################################");
            log.warn("전체 처리 시간: {} sec", stopWatch.getTotalTimeSeconds());
            log.warn("상세 리포트: \n{}", stopWatch.prettyPrint());
            log.warn("##################################################");
        }

        return ResponseEntity.ok("미정산 지급 처리 스케줄러 테스트");
    }


    // 5. 전체 테스트 용
    @PostMapping("/all")
    public ResponseEntity<String> testAll() {
        schedulerService.aggregateApprovedPayments();
        schedulerService.syncRefundedPayments();
        aggregationService.aggregate();
        schedulerService.executeSettlementPayout();
        return ResponseEntity.ok("모든 스케줄러 테스트");
    }

}
