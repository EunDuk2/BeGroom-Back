package com.example.BeGroom.settlement.util;

import com.example.BeGroom.common.util.QuerydslUtils;
import com.example.BeGroom.settlement.domain.QSettlement;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class QuerydslUtilsTest {

    @DisplayName("시작일과 종료일의 시점이 존재하는 경우 00:00:00과 23:59:59로 정규화한다.")
    @ParameterizedTest
    @CsvSource(value = {
            ", , null",
            "2026-01-01, , settlement.createdAt >= 2026-01-01T00:00",
            ", 2026-01-26, settlement.createdAt <= 2026-01-26T23:59:59",
            "2026-01-01, 2026-01-26, settlement.createdAt between 2026-01-01T00:00 and 2026-01-26T23:59:59"
    }, nullValues = "null")
    void dateNormalization(LocalDate start, LocalDate end, String expectedExpression) {
        // given
        DateTimePath<LocalDateTime> path = QSettlement.settlement.createdAt;

        // when
        BooleanExpression result = QuerydslUtils.dateBetween(path, start, end);

        // then
        if(expectedExpression == null){
            assertThat(result).isNull();
        }else{
            assertThat(result.toString()).isEqualTo(expectedExpression);
        }

    }

}
