package com.example.BeGroom.settlement.service;

import com.example.BeGroom.settlement.domain.PeriodType;
import com.example.BeGroom.settlement.dto.res.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

public interface SettlementService {

    // 정산 요약 정보 조회
    SettlementManageResDto getSettlementManage(Long sellerId);
    // 건별 정산 집계
    Page<ProductSettlementResDto> getProductSettlement(Long sellerId, LocalDate startDate, LocalDate endDate, int page);
    // 일별 정산 집계 조회
    Page<DailySettlementResDto> getDailySettlement(Long sellerId, int page);
    // 주차별 정산 집계 조회
    Page<WeeklySettlementResDto> getWeeklySettlement(Long sellerId, int page);
    // 월별 정산 집계 조회
    Page<MonthlySettlementResDto> getMonthlySettlement(Long sellerId, int page);
    // 연도별 정산 집계 조회
    Page<YearlySettlementResDto> getYearlySettlement(Long sellerId, int page);

    // 결제 승인 데이터 정산
    void aggregateApprovedPayments();
    // 정산 후, 환불 데이터 반영
    void syncRefundedPayments();

    // 지급 실행
    void executeSettlementPayout();

    // csv 내보내기
    void writeDailySettlementCsv(Long sellerId, PrintWriter writer)throws IOException;
}
