package com.example.BeGroom.settlement.service;

import com.example.BeGroom.settlement.domain.PeriodType;
import com.example.BeGroom.settlement.dto.res.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface SettlementService {

    // 정산 요약 정보 조회
    SettlementManageResDto getSettlementManage(Long sellerId);
    // 건별 정산 집계
    Page<ProductSettlementResDto> getProductSettlement(Long sellerId, LocalDate startDate, LocalDate endDate, int page);
    // 일별 정산 집계 조회
    Page<DailySettlementResDto> getDailySettlement(Long memberId, int page);
    // 주차별 정산 집계 조회
    Page<WeeklySettlementResDto> getWeeklySettlement(Long memberId, int page);
    // 월별 정산 집계 조회
    Page<MonthlySettlementResDto> getMonthlySettlement(Long memberId, int page);
    // 연도별 정산 집계 조회
    Page<YearlySettlementResDto> getYearlySettlement(Long memberId, int page);
}
