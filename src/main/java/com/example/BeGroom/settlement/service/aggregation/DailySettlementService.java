package com.example.BeGroom.settlement.service.aggregation;

import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.repository.daily.DailySettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailySettlementService implements SettlementAggregator {

    private final DailySettlementRepository dailySettlementRepository;

    @Override
    public void aggregate(Settlement settlement){
        LocalDate date = LocalDate.from(settlement.getDate());
        dailySettlementRepository.upsert(
                date,
                settlement.getSeller().getId(),
                settlement.getPaymentAmount(),
                settlement.getFeeRate(),
                settlement.getSettlementAmount(),
                settlement.getRefundAmount()
        );
    }

    @Override
    public void refund(Settlement settlement){
        dailySettlementRepository.updateRefund(
                settlement.getRefundAmount()
        );
    }
}
