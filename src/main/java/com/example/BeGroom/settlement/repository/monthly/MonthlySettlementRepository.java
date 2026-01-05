package com.example.BeGroom.settlement.repository.monthly;

import com.example.BeGroom.settlement.domain.MonthlySettlement;
import com.example.BeGroom.settlement.domain.id.MonthlySettlementId;
import com.example.BeGroom.settlement.dto.res.MonthlySettlementResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlySettlementRepository extends JpaRepository<MonthlySettlement, MonthlySettlementId>, MonthlySettlementRepositoryCustom {

    // 월별 정산 집계 조회
    @Query("""
        select new com.example.BeGroom.settlement.dto.res.MonthlySettlementResDto(
            ms.id.year,
            ms.id.month,
            ms.startDate,
            ms.endDate,
            ms.paymentAmount,
            ms.fee,
            ms.settlementAmount
        )
        from MonthlySettlement ms
        where ms.id.sellerId = :sellerId
        order by ms.id.year desc,
                ms.id.month desc
    """)
    Page<MonthlySettlementResDto> findMonthlySettlement(@Param("sellerId") Long sellerId, Pageable pageable);
}
