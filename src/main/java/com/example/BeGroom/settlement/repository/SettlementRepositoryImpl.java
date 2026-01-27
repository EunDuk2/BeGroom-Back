package com.example.BeGroom.settlement.repository;

import com.example.BeGroom.common.util.QuerydslUtils;
import com.example.BeGroom.settlement.domain.QDailySettlement;
import com.example.BeGroom.settlement.domain.QSettlement;
import com.example.BeGroom.settlement.domain.Settlement;
import com.example.BeGroom.settlement.dto.res.DailySettlementCsvDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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

    // 건별 정산 조회
    @Override
    public Page<Settlement> findProductSettlementListBySeller(
            Long sellerId, LocalDate startDate, LocalDate endDate, Pageable pageable
    ){
        QSettlement s = QSettlement.settlement;

        List<Settlement> content = queryFactory
                .selectFrom(s)
                .where(
                        s.seller.id.eq(sellerId),
                        QuerydslUtils.dateBetween(s.createdAt, startDate, endDate)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(s.count())
                .from(s)
                .where(
                        s.seller.id.eq(sellerId),
                        QuerydslUtils.dateBetween(s.createdAt, startDate, endDate)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }


    // 일별 정산 csv 다운로드
    @Override
    public List<DailySettlementCsvDto> findAllDailySettlementBySeller(Long sellerId){

        QDailySettlement d = QDailySettlement.dailySettlement;

        return queryFactory
                .select(Projections.constructor(
                        DailySettlementCsvDto.class,
                        d.id.date,
                        d.paymentAmount,
                        d.fee,
                        d.settlementAmount,
                        d.refundAmount
                ))
                .from(d)
                .where(d.id.sellerId.eq(sellerId))
                .orderBy(d.id.date.asc())
                .fetch();

        // 한번에 가져오는 fetch() 대신 stream(),
        // chunk 단위 조회(limit/offset),
        // 비동기 csv 생성 후 다운로드 링크 제공
    }
}
