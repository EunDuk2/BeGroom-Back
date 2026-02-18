package com.example.BeGroom.settlement.service.aggregation;

import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.SettlementPaymentStatus;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import io.micrometer.core.annotation.Timed;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.BeGroom.settlement.domain.SettlementPaymentStatus.REFUND;
import static com.example.BeGroom.settlement.domain.SettlementStatus.SETTLED;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final SettlementRepository settlementRepository;
    private final DailySettlementService dailySettlementService;
    private final WeeklySettlementService weeklySettlementService;
    private final MonthlySettlementService monthlySettlementService;
    private final YearlySettlementService yearlySettlementService;

    @Timed(value = "settlement.process.time", extraTags = {"step", "3_period-aggregation"})
    @Transactional
    public void aggregate() {
        // 미집계 데이터 조회
        // todo: heap 터짐
        // todo: boolean 값을 기준으로 들고오면 (index도 없으면) 최대 table full scan
        // todo: 만약 index 있는데도 isAgregated를 기준으로 들고 오면 (날짜범위 기준 없으면) index full scan
        List<Settlement> unaggregated = settlementRepository.findByStatusAndIsAggregatedFalse(SETTLED);

        //TODO: 잘 쪼개새요
//        List<Boolean> list1 = unaggregated.stream().map(u -> u.getStatus().equals(REFUND)).toList();
//        List<Boolean> list2 = unaggregated.stream().map(u -> u.getStatus().equals(REFUND)).toList();
//
        //TODO: 시간복잡도 o(n), db i/o시간이 비레함, 커넥션 시간도 비례함 ㅠ
        for(Settlement settlement : unaggregated){
            if(settlement.getSettlementPaymentStatus() == REFUND){
                updateRefund(settlement);
            }else {
                insertPayment(settlement);//결제테이블에서 가져옴
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
