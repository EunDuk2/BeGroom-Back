package com.example.BeGroom.seller.service;

import com.example.BeGroom.seller.domain.PeriodType;
import com.example.BeGroom.seller.domain.SettlementStatus;
import com.example.BeGroom.seller.dto.res.PeriodSettlementResDto;
import com.example.BeGroom.seller.dto.res.SettlementManageResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService{

    // 정산관리 조회
    @Override
    public SettlementManageResDto getSettlementManage(Long sellerId){
        // 결제금액, 환불금액, 수수료, 정산금액
        SettlementManageResDto.Summary summary =
                new SettlementManageResDto.Summary(230000, 66000, 223400, 2010600);
        // 건별 정산 리스트
        List<SettlementManageResDto.SettlementByItem> settlementByItemList = List.of();

        return new SettlementManageResDto(summary, settlementByItemList);
    }

    // 기간별 정산 집계
    @Override
    public List<PeriodSettlementResDto> getPeriodSettlement(Long sellerId, PeriodType type){
        return switch (type){
            case DAILY -> getDailySettlements(sellerId);
            case WEEKLY -> getWeeklySettlements(sellerId);
            case MONTHLY -> getMonthlySettlements(sellerId);
            case YEARLY -> getYearlySettlements(sellerId);
        };
    }

    // 일 정산 집계
    private List<PeriodSettlementResDto> getDailySettlements(Long sellerId){
        List<PeriodSettlementResDto> dailySettlements = List.of();
        return dailySettlements;
    }
    // 주 정산 집계
    private List<PeriodSettlementResDto> getWeeklySettlements(Long sellerId){
        List<PeriodSettlementResDto> weeklySettlements = List.of();
        return weeklySettlements;
    }
    // 월 정산 집계
    private List<PeriodSettlementResDto> getMonthlySettlements(Long sellerId){
        List<PeriodSettlementResDto> monthlySettlements = List.of();
        return monthlySettlements;
    }
    // 년 정산 집계
    private List<PeriodSettlementResDto> getYearlySettlements(Long sellerId){
        List<PeriodSettlementResDto> yearlySettlements = List.of();
        return yearlySettlements;
    }
}
