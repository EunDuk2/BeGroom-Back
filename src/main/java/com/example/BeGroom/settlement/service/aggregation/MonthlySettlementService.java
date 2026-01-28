package com.example.BeGroom.settlement.service.aggregation;

import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.repository.monthly.MonthlySettlementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MonthlySettlementService implements SettlementAggregator{

    private final MonthlySettlementRepository monthlySettlementRepository;

    @Transactional
    @Override
    public void aggregate(Settlement settlement){
        LocalDate originDate = settlement.getPayoutDate().toLocalDate();;
        // 연도, 월
        int year = originDate.getYear();
        int month = originDate.getMonthValue();

        // 해당 월의 1일
        LocalDate monthStart = originDate.withDayOfMonth(1);
        // 해당 월의 막날
        LocalDate monthEnd = originDate.withDayOfMonth(originDate.getDayOfMonth());

        monthlySettlementRepository.upsertAggregate(
                year,
                month,
                settlement.getSeller().getId(),
                settlement.getPaymentAmount(),
                settlement.getFee(),
                settlement.getSettlementAmount(),
                settlement.getRefundAmount(),
                monthStart,
                monthEnd
        );
    }

    @Transactional
    @Override
    public void refund(Settlement settlement){
        LocalDate originDate = settlement.getPayoutDate().toLocalDate();;
        // 연도, 월
        int year = originDate.getYear();
        int month = originDate.getMonthValue();

        monthlySettlementRepository.updateRefund(
                year,
                month,
                settlement.getSeller().getId(),
                settlement.getFee(),
                settlement.getRefundAmount()
        );
    }

}
