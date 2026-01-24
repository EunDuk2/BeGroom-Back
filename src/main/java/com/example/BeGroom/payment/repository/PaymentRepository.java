package com.example.BeGroom.payment.repository;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.seller.dto.res.RecentActivityResDto;
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

    // 판매자의 최근 환불
//    @Query("""
//        select new com.example.BeGroom.seller.dto.res.RecentActivityResDto.RecentRefundDto(
//            p.id,
//            s.refundAmount,
//            s.createdAt
//        )
//        from Settlement s
//        join s.payment p
//        where s.seller.id = :sellerId
//            and s.refundAmount > 0
//        order by s.createdAt desc
//    """)
//    List<RecentActivityResDto.RecentRefundDto> findLatestRefundBySeller(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query(value = """
    select 
        p.id as paymentId,
        s.refund_amount as refundAmount,
        s.created_at as createdAt
    from settlement s
    join payment p on s.payment_id = p.id
    where s.seller_id = :sellerId
      and s.refund_amount > 0
    order by s.created_at desc
    limit 1
""", nativeQuery = true)
    List<RecentRefundProjection> findLatestRefundBySeller(@Param("sellerId") Long sellerId);

    // 정산되지 않은 결제 승인 데이터
    @Query("""
    select p
    from Payment p
    where p.paymentStatus = com.example.BeGroom.payment.domain.PaymentStatus.APPROVED
        and p.isSettled = false
    """)
    List<Payment> findApprovedPayments();

}
