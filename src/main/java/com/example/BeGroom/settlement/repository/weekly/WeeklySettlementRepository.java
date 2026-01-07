package com.example.BeGroom.settlement.repository.weekly;

import com.example.BeGroom.settlement.domain.WeeklySettlement;
import com.example.BeGroom.settlement.domain.id.WeeklySettlementId;
import com.example.BeGroom.settlement.dto.res.WeeklySettlementResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklySettlementRepository extends JpaRepository<WeeklySettlement, WeeklySettlementId>, WeeklySettlementRepositoryCustom {

    // 주차별 정산 집계 조회
    @Query("""
        select new com.example.BeGroom.settlement.dto.res.WeeklySettlementResDto(
            ws.id.year,
            ws.id.month,
            ws.id.week,
            ws.startDate,
            ws.endDate,
            ws.paymentAmount,
            ws.fee,
            ws.settlementAmount
        )
        from WeeklySettlement ws
        where ws.id.sellerId = :sellerId
        order by ws.id.year desc,
                ws.id.month desc,
                ws.id.week desc
    """)
    Page<WeeklySettlementResDto> findWeeklySettlement(@Param("sellerId") Long sellerId, Pageable pageable);
}
