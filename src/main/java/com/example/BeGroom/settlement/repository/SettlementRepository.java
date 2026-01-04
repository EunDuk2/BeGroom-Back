package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.seller.dto.res.RecentActivityResDto;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
//    List<Settlement> findByAggregatedFalse();
    List<Settlement> findByStatus(SettlementStatus unsettled);

    // 총 주문 수(환불 제외)
    @Query("select coalesce(sum(s.paymentAmount - s.refundAmount), 0) " +
            "from Settlement s " +
            "where s.seller.id = :sellerId " +
            "and s.paymentStatus = 'PAYMENT'")
    long sumSalesAmountBySeller(@Param("sellerId") Long sellerId);

    // 판매자의 최근 환불
    @Query("""
        select new com.example.BeGroom.seller.dto.res.RecentActivityResDto.RecentSettlementDto(
            s.id,
            s.settlementAmount,
            s.createdAt
        )
        from Settlement s
        where s.seller.id = :sellerId
            and s.status = 'SETTLED'
        order by s.createdAt desc
    """)
    List<RecentActivityResDto.RecentSettlementDto> findLatestSettledBySeller(@Param("sellerId") Long sellerId, Pageable pageable);
}
