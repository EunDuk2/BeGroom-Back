package com.example.BeGroom.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {

    WAIT("판매대기"),
    SALE("판매중"),
    SOLD_OUT("품절"),
    STOP("판매중지");

    private final String text;
}
