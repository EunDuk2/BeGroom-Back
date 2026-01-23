package com.example.BeGroom.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTemplate {

    ORDER_PRODUCT(1L, "상품 주문 접수"),
    ORDER_REFUND_COMPLETE(2L, "환불 완료 알림"),
    AD_FREE_CASH_EVENT(3L, "선착순 캐시 이벤트 광고 알림"),
    NOTICE_SERVICE_INSPECTION(4L, "서비스 점검 알림");

    private final Long id;
    private final String description;
}
