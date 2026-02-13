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

        List<Long> searchIds = getSearchIds(condition.keyword(), pageable);
        if (StringUtils.hasText(condition.keyword()) && (searchIds == null || searchIds.isEmpty())) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        var query = queryFactory
            .selectFrom(product)
            .leftJoin(product.brand, brand).fetchJoin()
            .where(
                product.deletedAt.isNull(),
                product.productStatus.in(ProductStatus.SALE, ProductStatus.SOLD_OUT),
                productIdIn(searchIds),
                excludeSoldOutCondition(condition.excludeSoldOut()),
                categoryExists(condition.categoryIds()),
                optionExists(condition)
            )
            .orderBy(product.id.desc());

        if (!StringUtils.hasText(condition.keyword())) {
            query.offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1);
        }

        List<Product> content = query.fetch();
        return checkNextPage(pageable, content);
    }

    @Override
    public List<BrandFilterResponse> findBrandsBySearchCondition(ProductSearchCondition condition) {
        List<Long> searchIds = getSearchIds(condition.keyword());
        if (StringUtils.hasText(condition.keyword()) && (searchIds == null || searchIds.isEmpty())) {
            return Collections.emptyList();
        }

        return queryFactory
            .select(Projections.constructor(
                BrandFilterResponse.class,
                brand.id,
                brand.name,
                product.id.count()
            ))
            .from(product)
            .innerJoin(product.brand, brand)
            .where(
                product.deletedAt.isNull(),
                product.productStatus.in(ProductStatus.SALE, ProductStatus.SOLD_OUT),
                productIdIn(searchIds),
                excludeSoldOutCondition(condition.excludeSoldOut()),
                categoryExists(condition.categoryIds()),
                optionExists(condition)
            )
            .groupBy(brand.id, brand.name)
            .orderBy(product.id.count().desc())
            .fetch();
    }

    private List<Long> getSearchIds(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) return null;

        String sql = "SELECT p.id FROM product p " +
            "FORCE INDEX (idx_product_name_fulltext) " +
            "WHERE MATCH(p.name) AGAINST (:keyword IN BOOLEAN MODE) " +
            "AND p.deleted_at IS NULL " +
            "ORDER BY MATCH(p.name) AGAINST (:keyword IN BOOLEAN MODE) DESC " +
            "LIMIT :limit OFFSET :offset";

        try {
            Query query = entityManager.createNativeQuery(sql)
                .setParameter("keyword", keyword)
                .setParameter("limit", pageable.getPageSize() + 1)
                .setParameter("offset", pageable.getOffset())
                .setHint("jakarta.persistence.query.timeout", 5000);

            @SuppressWarnings("unchecked")
            List<Object> results = query.getResultList();

            return results.stream()
                .map(id -> ((Number) id).longValue())
                .toList();

        } catch (Exception e) {  // 추가: 에러 처리
            log.error("FULLTEXT search failed for keyword '{}': {}", keyword, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Long> getSearchIds(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;

        String sql = "SELECT p.id FROM product p " +
            "FORCE INDEX (idx_product_name_fulltext) " +
            "WHERE MATCH(p.name) AGAINST (:keyword IN BOOLEAN MODE) " +
            "AND p.deleted_at IS NULL";

        try {
            Query query = entityManager.createNativeQuery(sql)
                .setParameter("keyword", keyword)
                .setHint("jakarta.persistence.query.timeout", 2000);  // 추가: 2초 타임아웃

            @SuppressWarnings("unchecked")
            List<Object> results = query.getResultList();

            return results.stream()
                .map(id -> ((Number) id).longValue())
                .toList();

        } catch (Exception e) {  // 추가: 에러 처리
            log.error("FULLTEXT search failed for keyword '{}': {}", keyword, e.getMessage());
            return Collections.emptyList();
        }
    }

    private BooleanExpression productIdIn(List<Long> ids) {
        return ids != null ? product.id.in(ids) : null;
    }

    private Slice<Product> checkNextPage(Pageable pageable, List<Product> content) {
        boolean hasNext = false;

        // 조회된 결과가 요청한 페이지 사이즈보다 크면 다음 페이지가 있는 것
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize()); // +1로 가져온 마지막 항목 제거
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression categoryExists(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return null;

        return JPAExpressions
            .selectOne()
            .from(productCategory)
            .where(productCategory.product.eq(product)
                .and(productCategory.category.id.in(categoryIds)))
            .exists();
    }

    private BooleanExpression optionExists(ProductSearchCondition condition) {
        if (!hasOptionFilter(condition)) return null;

        return JPAExpressions
            .selectOne()
            .from(productOptionMapping)
            .innerJoin(productOptionMapping.productOption, productOption)
            .where(productOptionMapping.productDetail.product.eq(product)
                .and(deliveryTypeIn(condition.deliveryTypes()))
                .and(packagingTypeIn(condition.packagingTypes())))
            .exists();
    }

    private boolean hasOptionFilter(ProductSearchCondition condition) {
        return (condition.deliveryTypes() != null && !condition.deliveryTypes().isEmpty()) ||
            (condition.packagingTypes() != null && !condition.packagingTypes().isEmpty());
    }
    // 카테고리 필터
    private BooleanExpression categoryIn(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }
        return productCategory.category.id.in(categoryIds);
    }

    // 품절 제외 필터
    private BooleanExpression excludeSoldOutCondition(Boolean excludeSoldOut) {
        if (Boolean.TRUE.equals(excludeSoldOut)) {
            return product.productStatus.eq(ProductStatus.SALE);
        }
        return null;
    }

    // 배송 타입 필터
    private BooleanExpression deliveryTypeIn(List<String> deliveryTypes) {
        if (deliveryTypes == null || deliveryTypes.isEmpty()) {
            return null;
        }
        return productOption.optionType.eq("delivery")
            .and(productOption.optionValue.in(deliveryTypes));
    }

    // 포장 타입 필터
    private BooleanExpression packagingTypeIn(List<String> packagingTypes) {
        if (packagingTypes == null || packagingTypes.isEmpty()) {
            return null;
        }
        return productOption.optionType.eq("packaging")
            .and(productOption.optionValue.in(packagingTypes));
    }
}
