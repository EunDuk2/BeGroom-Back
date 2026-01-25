package com.example.BeGroom.settlement.repository.yearly;

import com.example.BeGroom.settlement.domain.YearlySettlement;
import com.example.BeGroom.settlement.domain.id.YearlySettlementId;
import com.example.BeGroom.settlement.dto.res.YearlySettlementResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YearlySettlementRepository extends JpaRepository<YearlySettlement, YearlySettlementId>, YearlySettlementRepositoryCustom {

    // 연도별 정산 집계 조회
    @Query("""
        select new com.example.BeGroom.settlement.dto.res.YearlySettlementResDto(
            ys.id.year,
            ys.startDate,
            ys.endDate,
            ys.paymentAmount,
            ys.fee,
            ys.settlementAmount
        )
        from YearlySettlement ys
        where ys.id.sellerId = :sellerId
        order by ys.id.year desc
    """)
    Page<YearlySettlementResDto> findYearlySettlement(@Param("sellerId") Long sellerId, Pageable pageable);

    Optional<YearlySettlement> findById(YearlySettlementId yearlySettlementId);
}
