package com.example.BeGroom.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTemplate {

    ORDER_SINGLE_PRODUCT(1L, "단일 상품 주문 접수"),
    ORDER_MULTIPLE_PRODUCT(2L, "복수 상품 주문 접수"),
    ORDER_REFUND_RECEIPT(3L, "환불 접수 알림"),
    ORDER_REFUND_REJECT(4L, "환불 거절 알림"),
    ORDER_REFUND_COMPLETE(5L, "환불 완료 알림"),
    NOTICE_SERVICE_INSPECTION(6L, "서비스 점검 알림");

    private final Long id;
    private final String description;
}
