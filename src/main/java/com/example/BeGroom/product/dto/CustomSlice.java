package com.example.BeGroom.product.dto;

import org.springframework.data.domain.Slice;

import java.io.Serializable;
import java.util.List;

public record CustomSlice<T>(
    List<T> content,
    boolean hasNext,
    int numberOfElements
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static <T> CustomSlice<T> from(Slice<T> slice) {
        return new CustomSlice<>(
            slice.getContent(),
            slice.hasNext(),
            slice.getNumberOfElements()
        );
    }
}
