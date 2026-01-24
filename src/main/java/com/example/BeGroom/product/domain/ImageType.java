package com.example.BeGroom.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {

    MAIN("대표 이미지(썸네일)"),
    DETAIL("상세 이미지");

    private final String text;
}
