package com.example.BeGroom.settlement.service.aggregation;

import com.example.BeGroom.settlement.domain.PaymentStatus;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final SettlementRepository settlementRepository;
    private final DailySettlementService dailySettlementService;
    private final WeeklySettlementService weeklySettlementService;
    private final MonthlySettlementService monthlySettlementService;
    private final YearlySettlementService yearlySettlementService;

    @Transactional
    public void aggregate() {
        // 미집계 데이터 조회
        List<Settlement> unaggregated = settlementRepository.findByStatus(SettlementStatus.UNSETTLED);

        for(Settlement settlement : unaggregated){
            if(settlement.getPaymentStatus() == PaymentStatus.REFUND){
                updateRefund(settlement);
            }else {
                insertPayment(settlement);
            }
            // 집계 확인 처리
            settlement.markAggregated();
        }
    }

    // 결제된 데이터 insert
    private void insertPayment(Settlement settlement){
        dailySettlementService.aggregate(settlement);
        weeklySettlementService.aggregate(settlement);
        monthlySettlementService.aggregate(settlement);
        yearlySettlementService.aggregate(settlement);
    }

    // 환불된 데이터 update
    private void updateRefund(Settlement settlement){
        dailySettlementService.refund(settlement);
        weeklySettlementService.refund(settlement);
        monthlySettlementService.refund(settlement);
        yearlySettlementService.refund(settlement);
    }
}
