package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.settlement.domain.QSettlement;
import com.example.BeGroom.settlement.repository.projection.ProductSettlementListProjection;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class SettlementRepositoryImpl implements SettlementRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    QSettlement s = QSettlement.settlement;

//    @Override
//    Page<ProductSettlementListProjection> findProductSettlementListBySeller(
//            Long sellerId, LocalDate startDate, LocalDate endDate, Pageable pageable
//    ){
//        List<ProductSettlementListProjection> content =
//    }
}
