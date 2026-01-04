package com.example.BeGroom.settlement.service.aggregation;

import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.repository.daily.DailySettlementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DailySettlementService implements SettlementAggregator {

    private final DailySettlementRepository dailySettlementRepository;

    @Transactional
    @Override
    public void aggregate(Settlement settlement){
        LocalDate date = LocalDate.from(settlement.getDate());
        dailySettlementRepository.upsertAggregate(
                date,
                settlement.getSeller().getId(),
                settlement.getPaymentAmount(),
                settlement.getFee(),
                settlement.getSettlementAmount(),
                settlement.getRefundAmount()
        );
    }

    @Transactional
    @Override
    public void refund(Settlement settlement){
        LocalDate date = LocalDate.from(settlement.getDate());
        dailySettlementRepository.updateRefund(
                date,
                settlement.getSeller().getId(),
                settlement.getFee(),
                settlement.getRefundAmount()
        );
    }
}
