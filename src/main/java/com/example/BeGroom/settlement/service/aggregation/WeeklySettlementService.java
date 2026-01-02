package com.example.BeGroom.settlement.service.aggregation;

import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.repository.weekly.WeeklySettlementRepository;
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

    @Override
    public void aggregate(Settlement settlement){
        LocalDate originDate = LocalDate.from(settlement.getDate());
        // 연도, 월
        int year = originDate.getYear();
        int month = originDate.getMonthValue();
        // 주차
        WeekFields wf = WeekFields.of(DayOfWeek.MONDAY, 1);
        int week = originDate.get(wf.weekOfYear());

        // 해당 월의 1일 (2025-12-01)
        LocalDate monthStart = originDate.withDayOfMonth(1);
        // 해당 월의 막날 (2025-12-31)
        LocalDate monthEnd = originDate.withDayOfMonth(originDate.lengthOfMonth());

        // 원래 주 시작일 (2025-12-28)
        LocalDate rawWeekStart = originDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        // 원래 주 종료일 (2026-1-4)
        LocalDate rawWeekEnd = originDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 월을 기준으로 보정된 주 시작일
        LocalDate startDate = rawWeekStart.isBefore(monthStart) ? monthStart : rawWeekStart;
        // 월을 기준으로 보정된 주 종료일
        LocalDate endDate = rawWeekEnd.isAfter(monthEnd) ? monthEnd : rawWeekEnd;

        weeklySettlementRepository.upsert(
                year,
                month,
                week,
                settlement.getSeller().getId(),
                settlement.getPaymentAmount(),
                settlement.getFeeRate(),
                settlement.getSettlementAmount(),
                settlement.getRefundAmount(),
                startDate,
                endDate
        );
    }

    @Override
    public void refund(Settlement settlement){
        weeklySettlementRepository.updateRefund(
                settlement.getRefundAmount()
        );
    }

}
