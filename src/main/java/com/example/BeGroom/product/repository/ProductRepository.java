package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductStatus;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import com.example.BeGroom.product.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>, ProductRepositoryCustom {

    Page<Product> findByProductStatusIn(List<ProductStatus> statuses, Pageable pageable);
    default Page<Product> findAllByCondition(ProductSearchCondition condition, Pageable pageable) {
        return findAll(ProductSpecification.searchBy(condition), pageable);
    }
    Optional<Product> findByNo(Long productNo);

    // 판매자가 가진 총 상품 수
//    int countBySellerIdAndDeletedAtIsNull(Long sellerId);
//    int countByBrandSellerIdAndDeletedAtIsNull(Long sellerId);

    @Query(value = """
        select count(product_id)
        from product p
        join brand b on p.brand_id = b.brand_id
        where b.seller_id = :sellerId
          and p.deleted_at is null
    """, nativeQuery = true
    )
    int countBySellerId(@Param("sellerId") Long sellerId);
}
