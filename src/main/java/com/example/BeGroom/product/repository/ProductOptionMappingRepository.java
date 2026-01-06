package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.ProductOptionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionMappingRepository extends JpaRepository<ProductOptionMapping, Long> {

    List<ProductOptionMapping> findByProductDetail_ProductDetailId(Long productDetailId);
}