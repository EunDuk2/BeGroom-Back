package com.example.BeGroom.order.repository;

import com.example.BeGroom.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 판매자 총 주문 수
    @Query("select count(distinct o.id) " +
            "from Order o " +
            "join o.orderProductList op " +
            "join op.product p " +
            "join Payment pay on pay.order = o " +
            "where p.sellerId = :sellerId " +
            "and pay.paymentStatus = 'APPROVED'")
    int countCompletedOrdersBySeller(@Param("sellerId") Long sellerId);

}
