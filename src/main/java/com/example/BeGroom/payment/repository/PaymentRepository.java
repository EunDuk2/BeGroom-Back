package com.example.BeGroom.payment.repository;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.seller.dto.res.RecentPaymentResDto;
import com.example.BeGroom.seller.dto.res.RecentRefundResDto;
import com.example.BeGroom.seller.repository.projection.RecentRefundProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);

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

}
