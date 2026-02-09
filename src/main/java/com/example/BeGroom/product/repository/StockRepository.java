package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
      select s
      from Stock s
      where s.productDetail.id in :ids
      order by s.id
    """)
    List<Stock> findAllByProductDetailIdsForUpdate(@Param("ids") List<Long> ids);

}
