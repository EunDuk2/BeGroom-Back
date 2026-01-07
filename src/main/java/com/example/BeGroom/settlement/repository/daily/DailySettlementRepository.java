package com.example.BeGroom.settlement.repository.daily;

import com.example.BeGroom.settlement.domain.DailySettlement;
import com.example.BeGroom.settlement.domain.id.DailySettlementId;
import com.example.BeGroom.settlement.dto.res.DailySettlementResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DailySettlementRepository extends JpaRepository<DailySettlement, DailySettlementId>, DailySettlementRepositoryCustom {

    // 일별 정산 집계 조회
    @Query("""
        select new com.example.BeGroom.settlement.dto.res.DailySettlementResDto(
            ds.id.date,
            ds.paymentAmount,
            ds.fee,
            ds.settlementAmount
        )
        from DailySettlement ds
        where ds.id.sellerId = :sellerId
        order by ds.id.date desc
    """)
    Page<DailySettlementResDto> findDailySettlement(@Param("sellerId") Long sellerId, Pageable pageable);
}
