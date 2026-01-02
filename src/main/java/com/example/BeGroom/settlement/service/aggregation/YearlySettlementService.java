package com.example.BeGroom.settlement.service.aggregation;

import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.repository.yearly.YearlySettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class YearlySettlementService implements SettlementAggregator{

    private final YearlySettlementRepository yearlySettlementRepository;

    @Override
    public void aggregate(Settlement settlement){
        LocalDate originDate = LocalDate.from(settlement.getDate());
        // 연도
        int year = originDate.getYear();

        // 해당 연도의 1일
        LocalDate yearStart = originDate.withDayOfYear(1);
        // 해당 연도의 막날
        LocalDate yearEnd = originDate.withDayOfYear(originDate.lengthOfYear());

        yearlySettlementRepository.upsert(
                year,
                settlement.getSeller().getId(),
                settlement.getPaymentAmount(),
                settlement.getFeeRate(),
                settlement.getSettlementAmount(),
                settlement.getRefundAmount(),
                yearStart,
                yearEnd
        );
    }

    @Override
    public void refund(Settlement settlement){
        yearlySettlementRepository.updateRefund(
                settlement.getRefundAmount()
        );
    }

}
