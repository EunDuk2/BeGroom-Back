package com.example.BeGroom.product.repository;

import com.example.BeGroom.product.domain.ProductStatus;
import com.example.BeGroom.product.dto.BrandFilterResponse;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.BeGroom.product.domain.QBrand.brand;
import static com.example.BeGroom.product.domain.QProduct.product;
import static com.example.BeGroom.product.domain.QProductCategory.productCategory;
import static com.example.BeGroom.product.domain.QProductDetail.productDetail;
import static com.example.BeGroom.product.domain.QProductOptionMapping.productOptionMapping;
import static com.example.BeGroom.product.domain.QProductOption.productOption;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BrandFilterResponse> findBrandsBySearchCondition(ProductSearchCondition condition) {
        return queryFactory
            .select(Projections.constructor(
                BrandFilterResponse.class,
                brand.id,
                brand.name,
                product.id.countDistinct()
            ))
            .from(product)
            .innerJoin(product.brand, brand)
            .leftJoin(product.productCategories, productCategory)
            .leftJoin(product.productDetails, productDetail)
            .leftJoin(productDetail.optionMappings, productOptionMapping)
            .leftJoin(productOptionMapping.productOption, productOption)
            .where(
                product.deletedAt.isNull(),
                product.productStatus.in(ProductStatus.SALE, ProductStatus.SOLD_OUT),
                keywordContains(condition.getKeyword()),
                categoryIn(condition.getCategoryIds()),
                excludeSoldOutCondition(condition.getExcludeSoldOut()),
                deliveryTypeIn(condition.getDeliveryTypes()),
                packagingTypeIn(condition.getPackagingTypes())
            )
            .groupBy(brand.id, brand.name)
            .orderBy(product.id.countDistinct().desc())
            .fetch();
    }

    // 키워드 검색 (공백 제거)
    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        String cleanKeyword = keyword.replaceAll("\\s+", "");
        return Expressions.stringTemplate(
            "REPLACE({0}, ' ', '')",
            product.name
        ).contains(cleanKeyword);
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
