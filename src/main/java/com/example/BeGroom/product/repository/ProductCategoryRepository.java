package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    boolean existsByProductIdAndCategoryId(Long productId, Long categoryId);
    long countByProductId(Long productId);
}