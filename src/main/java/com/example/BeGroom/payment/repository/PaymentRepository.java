package com.example.BeGroom.payment.repository;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.seller.dto.res.RecentPaymentResDto;
import com.example.BeGroom.seller.dto.res.RecentRefundResDto;
import com.example.BeGroom.seller.repository.projection.RecentRefundProjection;
import com.example.BeGroom.settlement.dto.res.SettlementTargetDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);
    List<Payment> findByOrderId(Long orderId);


    // 판매자의 최근 주문
    @Query("""
        select new com.example.BeGroom.seller.dto.res.RecentPaymentResDto(
                o.id,
                o.totalAmount,
                pay.approvedAt
                )
        from Order o
        join o.orderProductList op
        join op.productDetail pd
        join pd.product p
        join o.payments pay
        join p.brand b
        where b.seller.id = :sellerId
            and pay.paymentStatus = 'APPROVED'
        order by pay.approvedAt desc
    """)
    List<RecentPaymentResDto> findLatestOrderBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);


    // 판매자의 최근 환불
    @Query("""
        select new com.example.BeGroom.seller.dto.res.RecentRefundResDto(
            p.id,
            s.refundAmount,
            s.createdAt
        )
        from Settlement s
        right join s.payment p
        where s.seller.id = :sellerId
            and p.paymentStatus = :status
        order by s.createdAt desc
    """)
    List<RecentRefundResDto> findLatestRefundBySellerId(@Param("sellerId") Long sellerId, @Param("status") PaymentStatus status, Pageable pageable);

    // 정산되지 않은 결제 승인 데이터
    @Query("""
    select p
    from Payment p
    where p.paymentStatus = com.example.BeGroom.payment.domain.PaymentStatus.APPROVED
        and p.isSettled = false
    """)
    List<Payment> findApprovedPayments();

    // 정산되지 않은 결제 승인 데이터 - lastId 보다 큰 데이터를 가져옴
//    @Query("""
//    select p
//    from Payment p
//    where p.id > :lastId
//        and p.paymentStatus = com.example.BeGroom.payment.domain.PaymentStatus.APPROVED
//        order by p.id asc
//    """)
    @Query("""
    select new com.example.BeGroom.settlement.dto.res.SettlementTargetDto(
        p.id,
        p.amount,
        s.id
        )
    from Payment p
    join p.order o
    join o.orderProductList op
    join op.productDetail pd
    join pd.product pr
    join pr.brand b
    join b.seller s
    where p.id > :lastId
        and p.paymentStatus = com.example.BeGroom.payment.domain.PaymentStatus.APPROVED
        and p.isSettled = false
    order by p.id asc
    """)
    Slice<SettlementTargetDto> findPaymentForSettlement(@Param("lastId") Long lastId, Pageable pageable);

    // @Modifying : 변경 쿼리임을 선언. dirty Checking 거치지 않음, 영속성 컨텍스트를 거치지 않아 CPU와 메모리 부하 감소
    @Modifying
    @Query("""
    update Payment p
    set p.isSettled = true
    where p.id in :ids
    """)
    void updateSettledStatusByIds(@Param("ids") List<Long> ids);
}
