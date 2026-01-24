package com.example.BeGroom.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {

    BASIC("기본 카테고리"),
    SEASON("시즌 카테고리"),
    EVENT("이벤트"),
    BEST("베스트"),
    DISCOUNT("할인특가");

    private final String text;
}
