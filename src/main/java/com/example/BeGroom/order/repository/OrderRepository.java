package com.example.BeGroom.order.repository;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.seller.repository.projection.OrderListProjection;
import com.example.BeGroom.seller.repository.projection.RecentOrderProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByMemberId(Pageable pageable, Long memberId);
  
    public List<Order> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.orderProductList op " +
            "JOIN FETCH op.product p " +
            "WHERE o.member.id = :memberId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findAllWithDetailsByMemberId(@Param("memberId") Long memberId);

    // 판매자 총 주문 수
//    @Query("""
//        select count(distinct o.id)
//        from Order o
//        join o.orderProductList op
//        join op.product p
//        join o.payments pay
//        join p.brand b
//        where b.sellerId = :sellerId
//          and pay.paymentStatus = 'APPROVED'
//    """)
    @Query(value = """
        select count(distinct o.id)
        from orders o
        join order_product op on o.id = op.order_id
        join product p on op.product_id = p.product_id
        join brand b on p.brand_id = b.brand_id
        join payment pay on pay.order_id = o.id
        where b.seller_id = :sellerId
            and pay.payment_status = 'APPROVED'
    """, nativeQuery = true)
    int countCompletedOrdersBySeller(@Param("sellerId") Long sellerId);

    // 판매자의 최근 주문
//    @Query("""
//        select new com.example.BeGroom.seller.dto.res.RecentActivityResDto.RecentOrderDto(
//                o.id,
//                o.totalAmount,
//                pay.approvedAt
//                )
//        from Order o
//        join o.orderProductList op
//        join op.product p
//        join o.payments pay
//        join p.brand b
//        where b.sellerId = :sellerId
//            and pay.paymentStatus = 'APPROVED'
//        order by pay.approvedAt desc
//    """)
//    List<RecentActivityResDto.RecentOrderDto> findLatestOrderBySeller(@Param("sellerId") Long sellerId, Pageable pageable);
    @Query(value = """
        select
            o.id as orderId,
            o.total_amount as totalAmount,
            pay.approved_at as approvedAt
        from orders o
        join order_product op on o.id = op.order_id
        join product p on op.product_id = p.product_id
        join brand b on p.brand_id = b.brand_id
        join payment pay on pay.order_id = o.id
        where b.seller_id = :sellerId
            and pay.payment_status = 'APPROVED'
        order by pay.approved_at desc
    """, nativeQuery = true)
    List<RecentOrderProjection> findLatestOrderBySeller(@Param("sellerId") Long sellerId, Pageable pageable);

    // 판매자의 주문 목록
//    @Query(value = """
//        select distinct new com.example.BeGroom.seller.dto.res.OrderListResDto(
//            o.id,
//            o.createdAt,
//            o.totalAmount,
//            pay.paymentMethod,
//            s.status,
//            pay.paymentStatus
//        )
//        from Order o
//        join o.orderProductList op
//        join op.product p
//        join o.payments pay
//        join p.brand b
//        left join Settlement s
//            on s.payment = pay
//        where b.sellerId = :sellerId
//        order by o.createdAt desc
//    """,
//    countQuery = """
//        select count(distinct o.id)
//        from Order o
//        join o.orderProductList op
//        join op.product p
//        join p.brand b
//        where b.sellerId = :sellerId
//    """)
//    Page<OrderListResDto> findOrderListBySeller(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT
            o.id            AS orderId,
            o.created_at    AS createdAt,
            o.total_amount  AS totalAmount,
            pay.payment_method AS paymentMethod,
            s.status        AS settlementStatus,
            pay.payment_status AS paymentStatus
        FROM orders o
        JOIN order_product op
            ON o.id = op.order_id
        JOIN product p
            ON op.product_id = p.product_id
        JOIN brand b
            ON p.brand_id = b.brand_id
        JOIN payment pay
            ON pay.order_id = o.id
        LEFT JOIN settlement s
            ON s.payment_id = pay.id
        WHERE b.seller_id = :sellerId
        ORDER BY o.created_at DESC
    """,
            countQuery = """
        SELECT COUNT(DISTINCT o.id)
        FROM orders o
        JOIN order_product op
            ON o.id = op.order_id
        JOIN product p
            ON op.product_id = p.product_id
        JOIN brand b
            ON p.brand_id = b.brand_id
        WHERE b.seller_id = :sellerId
    """, nativeQuery = true
    )
    Page<OrderListProjection> findOrderListBySeller(@Param("sellerId") Long sellerId, Pageable pageable);
}
