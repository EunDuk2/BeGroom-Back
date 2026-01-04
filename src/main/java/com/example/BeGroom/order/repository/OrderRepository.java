package com.example.BeGroom.order.repository;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.seller.dto.res.RecentActivityResDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 판매자 총 주문 수
    @Query("""
        select count(distinct o.id)
        from Order o
        join o.orderProductList op
        join op.product p
        join Brand b on b.brandId = p.brandId
        join Payment pay on pay.order = o
        where b.sellerId = :sellerId
          and pay.paymentStatus = 'APPROVED'
    """)
    int countCompletedOrdersBySeller(@Param("sellerId") Long sellerId);

    // 판매자의 최근 주문
    @Query("""
        select new com.example.BeGroom.seller.dto.res.RecentActivityResDto.RecentOrderDto(
                o.id,
                o.totalAmount,
                pay.approvedAt
                )
        from Order o
        join o.orderProductList op
        join op.product p
        join Brand b on b.brandId = p.brandId
        join Payment pay on pay.order = o
        where b.sellerId = :sellerId
            and pay.paymentStatus = 'APPROVED'
        order by pay.approvedAt desc
    """)
    List<RecentActivityResDto.RecentOrderDto> findLatestOrderBySeller(@Param("sellerId") Long sellerId, Pageable pageable);
}
