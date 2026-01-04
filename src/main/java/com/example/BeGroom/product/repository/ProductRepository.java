package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.Product;
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
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByProductStatusIn(List<Product.ProductStatus> statuses, Pageable pageable);
    Optional<Product> findByProductNo(Long productNo);

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



    // 특정 배송 타입을 가진 상품 ID 목록 조회
    @Query(value = """
    SELECT DISTINCT p.product_id
    FROM product p
    INNER JOIN product_detail pd ON p.product_id = pd.product_id
    INNER JOIN product_option_mapping pom ON pd.product_detail_id = pom.product_detail_id
    INNER JOIN product_option po ON pom.option_id = po.option_id
    WHERE po.option_type = 'delivery'
    AND po.option_value IN :deliveryTypes
    """, nativeQuery = true)
    List<Long> findProductIdsByDeliveryTypes(@Param("deliveryTypes") List<String> deliveryTypes);

    // 특정 포장 타입을 가진 상품 ID 목록 조회
    @Query(value = """
    SELECT DISTINCT p.product_id
    FROM product p
    INNER JOIN product_detail pd ON p.product_id = pd.product_id
    INNER JOIN product_option_mapping pom ON pd.product_detail_id = pom.product_detail_id
    INNER JOIN product_option po ON pom.option_id = po.option_id
    WHERE po.option_type = 'packaging'
    AND po.option_value IN :packagingTypes
    """, nativeQuery = true)
    List<Long> findProductIdsByPackagingTypes(@Param("packagingTypes") List<String> packagingTypes);

    // 카테고리별 상품 ID 조회 추가
    @Query(value = """
    SELECT DISTINCT p.product_id
    FROM product p
    INNER JOIN product_category pc ON p.product_id = pc.product_id
    WHERE pc.category_id IN :categoryIds
    """, nativeQuery = true)
    List<Long> findProductIdsByCategoryIds(@Param("categoryIds") List<Long> categoryIds);
}
