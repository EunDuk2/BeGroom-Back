package com.example.BeGroom.payment.repository;

import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.seller.dto.res.RecentActivityResDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);

    // 판매자의 최근 환불
    @Query("""
        select new com.example.BeGroom.seller.dto.res.RecentActivityResDto.RecentRefundDto(
            p.id,
            s.refundAmount,
            s.createdAt
        )
        from Settlement s
        join s.payment p
        where s.seller.id = :sellerId
            and s.refundAmount > 0
        order by s.createdAt desc
    """)
    List<RecentActivityResDto.RecentRefundDto> findLatestRefundBySeller(@Param("sellerId") Long sellerId, Pageable pageable);
}
