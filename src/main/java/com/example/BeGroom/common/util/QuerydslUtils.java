package com.example.BeGroom.common.util;

import com.example.BeGroom.settlement.domain.QSettlement;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;


import java.time.LocalDate;
import java.time.LocalDateTime;

public class QuerydslUtils {

    // 동적 날짜 정규화 메서드
    public static BooleanExpression dateBetween(
            DateTimePath<LocalDateTime> path, LocalDate start, LocalDate end){
        if(start == null && end == null) return null;

        if(start != null && end != null){
            return path.between(start.atStartOfDay(), end.atTime(23, 59, 59));
        }
        if(start != null){
            return path.goe(start.atStartOfDay());
        }
        return path.loe(end.atTime(23, 59, 59));
    }

}
