package com.example.BeGroom.settlement.service;

import com.example.BeGroom.settlement.domain.PeriodType;
import com.example.BeGroom.settlement.dto.req.ProductSettlementReqDto;
import com.example.BeGroom.settlement.dto.res.PeriodSettlementResDto;
import com.example.BeGroom.settlement.dto.res.ProductSettlementResDto;
import com.example.BeGroom.settlement.dto.res.SettlementManageResDto;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import com.example.BeGroom.settlement.repository.projection.ProductSettlementListProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;

    // 정산 요약 정보 조회
    @Override
    public SettlementManageResDto getSettlementManage(Long sellerId){
        // 결제금액
        Long totalPaymentAmount = settlementRepository.getTotalPaymentAmountBySeller(sellerId);
        // 환불금액
        BigDecimal totalRefundAmount = settlementRepository.getTotalRefundtAmountBySeller(sellerId);
        // 수수료
        BigDecimal totalFeeAmount = settlementRepository.getTotalFeeAmountBySeller(sellerId);
        // 정산금액
        BigDecimal totalSettlementAmount = settlementRepository.getTotalSettlementAmountBySeller(sellerId);

        return new SettlementManageResDto(
                totalPaymentAmount,
                totalRefundAmount,
                totalFeeAmount,
                totalSettlementAmount);
    }

    // 건별 정산 집계 조회
    @Override
    public Page<ProductSettlementResDto> getProductSettlement(Long sellerId, LocalDate startDate, LocalDate endDate, int page){
        Pageable pageable = PageRequest.of(page, 15);
        // 건별 정산 리스트
        Page<ProductSettlementListProjection> projectionPage =
                settlementRepository.findProductSettlementListBySeller(sellerId, startDate, endDate, pageable);

        return projectionPage.map(p -> new ProductSettlementResDto(

        ));
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
