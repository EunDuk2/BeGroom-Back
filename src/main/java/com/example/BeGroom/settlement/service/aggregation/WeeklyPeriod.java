package com.example.BeGroom.settlement.service.aggregation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class WeeklyPeriod {

    private final int year;
    private final int month;
    private final int week;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public static WeeklyPeriod calc(LocalDate originDate){
        // 연도, 월
        int year = originDate.getYear();
        int month = originDate.getMonthValue();
        // 주차
//        WeekFields wf = WeekFields.of(DayOfWeek.MONDAY, 1);
//        int week = originDate.get(wf.weekOfYear());

        // 해당 월의 1일 (2025-12-01)
        LocalDate monthStart = originDate.withDayOfMonth(1);
        // 해당 월의 막날 (2025-12-31)
        LocalDate monthEnd = originDate.withDayOfMonth(originDate.lengthOfMonth());

        // 월 기준 주차 계산
        // 해당 월의 1일을 기준으로 한 월요일을 1주차 기준점으로 설정
        LocalDate monthWeekStart = monthStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        // 7일 단위(1주)가 몇번 지나갔는지
        int week = (int)ChronoUnit.WEEKS.between(monthWeekStart, originDate) + 1;
        log.info(originDate + " : " + week);

        // 원래 주 시작일 (2025-12-28)
        LocalDate originWeekStart = originDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        // 원래 주 종료일 (2026-1-4)
        LocalDate originWeekEnd = originDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 월을 기준으로 보정된 주 시작일
        LocalDate startDate = originWeekStart.isBefore(monthStart) ? monthStart : originWeekStart;
        // 월을 기준으로 보정된 주 종료일
        LocalDate endDate = originWeekEnd.isAfter(monthEnd) ? monthEnd : originWeekEnd;

        return new WeeklyPeriod(year, month, week, startDate, endDate);
    }
}
