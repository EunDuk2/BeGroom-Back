package com.example.BeGroom.settlement.scheduler;

import com.example.BeGroom.settlement.service.aggregation.AggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementAggregationScheduler {

    private final AggregationService aggregationService;

    // 10초 마다 실행
    @Scheduled(fixedDelay = 10000)
    public void run(){
        aggregationService.aggregate();
    }
}
