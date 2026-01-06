package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProduct_ProductIdAndImageTypeOrderBySortOrderAsc(Long productId, ProductImage.ImageType imageType);
}