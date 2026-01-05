package com.example.BeGroom.product.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long productDetailId) {
        super("재고가 부족한 상품입니다. productDetailId=" + productDetailId);
    }
}

