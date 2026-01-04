package com.example.BeGroom.order.repository;

import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.dto.OrderProductAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    @Query("""
        select new com.example.BeGroom.order.dto.OrderProductAggregate(
            op.order.id,
            min(p.name),
            sum(op.quantity),
            count(op.id)
        )
        from OrderProduct op
        join op.product p
        where op.order.id in :orderIds
        group by op.order.id
    """)
    List<OrderProductAggregate> aggregateByOrderIds(
            @Param("orderIds") List<Long> orderIds
    );

    List<OrderProduct> findByOrderId(Long orderId);

}
