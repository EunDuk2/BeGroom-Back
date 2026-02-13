package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.dto.BrandFilterResponse;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import com.example.BeGroom.product.specification.ProductSpecification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ProductRepositoryCustom {
    List<BrandFilterResponse> findBrandsBySearchCondition(ProductSearchCondition condition);
    Slice<Product> findAllByCondition(ProductSearchCondition condition, Pageable pageable);
}
