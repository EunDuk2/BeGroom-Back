package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductStatus;
import com.example.BeGroom.product.dto.BrandFilterResponse;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

import static com.example.BeGroom.product.domain.QBrand.brand;
import static com.example.BeGroom.product.domain.QProduct.product;
import static com.example.BeGroom.product.domain.QProductCategory.productCategory;
import static com.example.BeGroom.product.domain.QProductDetail.productDetail;
import static com.example.BeGroom.product.domain.QProductOptionMapping.productOptionMapping;
import static com.example.BeGroom.product.domain.QProductOption.productOption;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public Slice<Product> findAllByCondition(ProductSearchCondition condition, Pageable pageable) {
        // 기본 쿼리 구성
        var query = queryFactory
            .selectFrom(product)
            .leftJoin(product.brand, brand).fetchJoin()
            .distinct();

        // JOIN 추가 (필요한 경우)
        if (hasCategory(condition)) {
            query.leftJoin(product.productCategories, productCategory);
        }

        if (hasOption(condition)) {
            query.leftJoin(product.productDetails, productDetail)
                .leftJoin(productDetail.optionMappings, productOptionMapping)
                .leftJoin(productOptionMapping.productOption, productOption);
        }

        // WHERE 조건
        query.where(
                product.deletedAt.isNull(),
                product.productStatus.in(ProductStatus.SALE, ProductStatus.SOLD_OUT),
                keywordLike(condition.keyword()),
                categoryIn(condition.categoryIds()),
                brandIn(condition.brandIds()),
                optionIn(condition),
                excludeSoldOut(condition.excludeSoldOut())
            )
            .orderBy(product.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1);

        List<Product> content = query.fetch();
        return toSlice(content, pageable);
    }

    @Override
    public List<BrandFilterResponse> findBrandsBySearchCondition(ProductSearchCondition condition) {
        var query = queryFactory
            .select(Projections.constructor(
                BrandFilterResponse.class,
                brand.id,
                brand.name,
                product.id.countDistinct()
            ))
            .from(product)
            .innerJoin(product.brand, brand);

        // JOIN 추가
        if (hasCategory(condition)) {
            query.leftJoin(product.productCategories, productCategory);
        }

        if (hasOption(condition)) {
            query.leftJoin(product.productDetails, productDetail)
                .leftJoin(productDetail.optionMappings, productOptionMapping)
                .leftJoin(productOptionMapping.productOption, productOption);
        }

        // WHERE 조건
        query.where(
                product.deletedAt.isNull(),
                product.productStatus.in(ProductStatus.SALE, ProductStatus.SOLD_OUT),
                keywordLike(condition.keyword()),
                categoryIn(condition.categoryIds()),
                optionIn(condition),
                excludeSoldOut(condition.excludeSoldOut())
            )
            .groupBy(brand.id, brand.name)
            .orderBy(product.id.countDistinct().desc());

        return query.fetch();
    }

    /**
     * 키워드 검색 (LIKE)
     */
    private BooleanExpression keywordLike(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return product.name.containsIgnoreCase(keyword);
    }

    /**
     * 카테고리 필터
     */
    private BooleanExpression categoryIn(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }
        return productCategory.category.id.in(categoryIds);
    }

    /**
     * 브랜드 필터
     */
    private BooleanExpression brandIn(List<Long> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) {
            return null;
        }
        return brand.id.in(brandIds);
    }

    /**
     * 옵션 필터 (배송/포장)
     */
    private BooleanExpression optionIn(ProductSearchCondition condition) {
        if (!hasOption(condition)) {
            return null;
        }

        BooleanExpression result = null;

        // 배송 타입
        if (condition.deliveryTypes() != null && !condition.deliveryTypes().isEmpty()) {
            BooleanExpression delivery = productOption.optionType.eq("delivery")
                .and(productOption.optionValue.in(condition.deliveryTypes()));
            result = delivery;
        }

        // 포장 타입 (OR)
        if (condition.packagingTypes() != null && !condition.packagingTypes().isEmpty()) {
            BooleanExpression packaging = productOption.optionType.eq("packaging")
                .and(productOption.optionValue.in(condition.packagingTypes()));
            result = result == null ? packaging : result.or(packaging);
        }

        return result;
    }

    /**
     * 품절 제외
     */
    private BooleanExpression excludeSoldOut(Boolean excludeSoldOut) {
        if (Boolean.TRUE.equals(excludeSoldOut)) {
            return product.productStatus.eq(ProductStatus.SALE);
        }
        return null;
    }

    /**
     * 카테고리 필터 존재 여부
     */
    private boolean hasCategory(ProductSearchCondition condition) {
        return condition.categoryIds() != null && !condition.categoryIds().isEmpty();
    }

    /**
     * 옵션 필터 존재 여부
     */
    private boolean hasOption(ProductSearchCondition condition) {
        return (condition.deliveryTypes() != null && !condition.deliveryTypes().isEmpty()) ||
            (condition.packagingTypes() != null && !condition.packagingTypes().isEmpty());
    }

    /**
     * Slice 변환
     */
    private Slice<Product> toSlice(List<Product> content, Pageable pageable) {
        boolean hasNext = content.size() > pageable.getPageSize();

        if (hasNext) {
            content.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
