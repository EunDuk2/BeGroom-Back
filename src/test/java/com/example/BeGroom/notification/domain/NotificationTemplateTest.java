package com.example.BeGroom.notification.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NotificationTemplateTest {

    @ParameterizedTest(name = "NotificationTemplate은 ID가 expectedId이고 설명이 expectedDescription이다.")
    @MethodSource("provideEnumData")
    void enum_mapping_test(NotificationTemplate template, Long expectedId, String expectedDescription) {
        assertThat(template.getId()).isEqualTo(expectedId);
        assertThat(template.getDescription()).isEqualTo(expectedDescription);
    }

    private static Stream<Arguments> provideEnumData() {
        return Stream.of(
                Arguments.of(NotificationTemplate.ORDER_PRODUCT, 1L, "상품 주문 접수"),
                Arguments.of(NotificationTemplate.ORDER_REFUND_COMPLETE, 2L, "환불 완료 알림"),
                Arguments.of(NotificationTemplate.AD_FREE_CASH_EVENT, 3L, "선착순 캐시 이벤트 광고 알림"),
                Arguments.of(NotificationTemplate.NOTICE_SERVICE_INSPECTION, 4L, "서비스 점검 알림")
        );
    }
}