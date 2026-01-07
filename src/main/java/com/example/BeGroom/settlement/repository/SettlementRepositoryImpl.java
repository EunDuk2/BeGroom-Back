package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.settlement.domain.QSettlement;
import com.example.BeGroom.settlement.domain.Settlement;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class SettlementRepositoryImpl implements SettlementRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    QSettlement s = QSettlement.settlement;

    @Override
    public Page<Settlement> findProductSettlementListBySeller(
            Long sellerId, LocalDate startDate, LocalDate endDate, Pageable pageable
    ){

        // 동적 날짜 조건
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<Settlement> content = queryFactory
                .selectFrom(s)
                .where(
                        s.seller.id.eq(sellerId),
                        start != null && end != null ? s.createdAt.between(start, end) : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(s.count())
                .from(s)
                .where(
                        s.seller.id.eq(sellerId),
                        start != null && end != null ? s.createdAt.between(start, end) : null
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
