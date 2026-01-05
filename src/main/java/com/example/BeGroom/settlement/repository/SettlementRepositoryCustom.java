package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.settlement.domain.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface SettlementRepositoryCustom {
    // 건별 정산 목록
    Page<Settlement> findProductSettlementListBySeller(
            Long sellerId, LocalDate startDate, LocalDate endDate, Pageable pageable
    );
}
