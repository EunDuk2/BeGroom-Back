package com.example.BeGroom.order.domain;

import com.example.BeGroom.product.domain.ProductDetail;

public class OrderLineRequest {
    private final ProductDetail productDetail;
    private final int quantity;

    public OrderLineRequest(ProductDetail productDetail, int quantity) {
        this.productDetail = productDetail;
        this.quantity = quantity;
    }

    public ProductDetail productDetail() {
        return productDetail;
    }

    public int quantity() {
        return quantity;
    }
}
