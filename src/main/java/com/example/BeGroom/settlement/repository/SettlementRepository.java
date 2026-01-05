package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.seller.repository.projection.RecentSettlementProjection;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.domain.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long>, SettlementRepositoryCustom {
//    List<Settlement> findByAggregatedFalse();
    List<Settlement> findByStatus(SettlementStatus unsettled);

    // 총 주문 수(환불 제외)
    @Query("select coalesce(sum(s.paymentAmount - s.refundAmount), 0) " +
            "from Settlement s " +
            "where s.seller.id = :sellerId " +
            "and s.paymentStatus = 'PAYMENT'")
    long sumSalesAmountBySeller(@Param("sellerId") Long sellerId);

    // 판매자의 최근 환불
//    @Query("""
//        select new com.example.BeGroom.seller.dto.res.RecentActivityResDto.RecentSettlementDto(
//            s.id,
//            s.settlementAmount,
//            s.createdAt
//        )
//        from Settlement s
//        where s.seller.id = :sellerId
//            and s.status = 'SETTLED'
//        order by s.createdAt desc
//    """)
//    List<RecentActivityResDto.RecentSettlementDto> findLatestSettledBySeller(@Param("sellerId") Long sellerId, Pageable pageable);
    @Query(value = """
    select
        s.id as settlementId,
        s.settlement_amount as settlementAmount,
        s.created_at as createdAt
    from settlement s
    where s.seller_id = :sellerId
      and s.status = 'SETTLED'
    order by s.created_at desc
    limit 1
""", nativeQuery = true)
    List<RecentSettlementProjection> findLatestSettledBySeller(@Param("sellerId") Long sellerId);



    // 판매자의 총 환불 건수
    @Query("""
        select count(s)
        from Settlement s
        where s.seller.id = :sellerId
            and s.refundAmount > 0
    """)
    int countRefundBySeller(@Param("sellerId") Long sellerId);

    // 판매자의 정산 대기 건수
    @Query("""
        select count(s)
        from Settlement s
        where s.seller.id = :sellerId
            and s.status = 'UNSETTLED'
    """)
    int countUnsettledBySeller(@Param("sellerId") Long sellerId);

    // 주문 목록

//    List<OrderListResDto> getOrderList(@Param("sellerId") Long sellerId);

    // 결제금액
    @Query("""
        select coalesce(sum(s.paymentAmount), 0)
        from Settlement s
        where s.seller.id = :sellerId
    """)
    Long getTotalPaymentAmountBySeller(@Param("sellerId") Long sellerId);

    // 환불금액
    @Query("""
        select coalesce(sum(s.refundAmount), 0)
        from Settlement s
        where s.seller.id = :sellerId
    """)
    BigDecimal getTotalRefundtAmountBySeller(Long sellerId);

    // 수수료
    @Query("""
        select coalesce(sum(s.fee), 0)
        from Settlement s
        where s.seller.id = :sellerId
    """)
    BigDecimal getTotalFeeAmountBySeller(Long sellerId);

    // 정산금액
    @Query("""
        select coalesce(sum(s.settlementAmount), 0)
        from Settlement s
        where s.seller.id = :sellerId
    """)
    BigDecimal getTotalSettlementAmountBySeller(Long sellerId);
}
