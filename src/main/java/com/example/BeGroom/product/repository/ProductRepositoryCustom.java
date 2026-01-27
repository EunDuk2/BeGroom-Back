package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.dto.BrandFilterResponse;
import com.example.BeGroom.product.dto.ProductSearchCondition;

import java.util.List;

public interface ProductRepositoryCustom {
    List<BrandFilterResponse> findBrandsBySearchCondition(ProductSearchCondition condition);
}
