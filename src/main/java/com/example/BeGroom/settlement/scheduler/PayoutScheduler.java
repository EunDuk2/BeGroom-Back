package com.example.BeGroom.settlement.scheduler;

import com.example.BeGroom.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayoutScheduler {

    private final SettlementService settlementService;

    // 10초마다 미정산 지급 실행
    @Scheduled(fixedDelay = 10000)
    public void run(){
        settlementService.executeSettlementPayout();
    }
}
