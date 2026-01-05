package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.settlement.repository.projection.ProductSettlementListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface SettlementRepositoryCustom {
    Page<ProductSettlementListProjection> findProductSettlementListBySeller(
            Long sellerId, LocalDate startDate, LocalDate endDate, Pageable pageable
    );
}
