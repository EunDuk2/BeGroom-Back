package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.dto.res.DailySettlementCsvDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface SettlementRepositoryCustom {
    // 건별 정산 목록
    Page<Settlement> findProductSettlementListBySeller(
            Long sellerId, LocalDate startDate, LocalDate endDate, Pageable pageable
    );

    // 일별 정산 집계 csv 다운로드
    List<DailySettlementCsvDto> findAllDailySettlementBySeller(Long sellerId);
}
