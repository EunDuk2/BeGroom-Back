package com.example.BeGroom.settlement.service.aggregation;

import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.repository.weekly.WeeklySettlementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

@Service
@RequiredArgsConstructor
public class WeeklySettlementService implements SettlementAggregator{

    private final WeeklySettlementRepository weeklySettlementRepository;

    @Transactional
    @Override
    public void aggregate(Settlement settlement){
        // 년, 월, 주차 계산
        WeeklyPeriod period = WeeklyPeriod.calc(settlement.getCreatedAt().toLocalDate());

        weeklySettlementRepository.upsertAggregate(
                period.getYear(),
                period.getMonth(),
                period.getWeek(),
                settlement.getSeller().getId(),
                settlement.getPaymentAmount(),
                settlement.getFee(),
                settlement.getSettlementAmount(),
                settlement.getRefundAmount(),
                period.getStartDate(),
                period.getEndDate()
        );
    }

    @Transactional
    @Override
    public void refund(Settlement settlement){
        // 년, 월, 주차 계산
        WeeklyPeriod period = WeeklyPeriod.calc(settlement.getCreatedAt().toLocalDate());

        weeklySettlementRepository.updateRefund(
                period.getYear(),
                period.getMonth(),
                period.getWeek(),
                settlement.getSeller().getId(),
                settlement.getFee(),
                settlement.getRefundAmount()
        );
    }

}
