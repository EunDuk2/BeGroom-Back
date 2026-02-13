package com.example.BeGroom.product.specification;

import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductStatus;
import com.example.BeGroom.product.dto.ProductSearchCondition;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductSpecification {

    /**
     * 상품 검색 통합 Specification
     */
    public static Specification<Product> searchBy(ProductSearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 0. 삭제 상품 제외 (인덱스 활용을 위해 상단 배치)
            predicates.add(cb.isNull(root.get("deletedAt")));

            // 기본 상태 필터
            predicates.add(root.get("productStatus").in(ProductStatus.SALE, ProductStatus.SOLD_OUT));

            // 1. 키워드 검색
            if (StringUtils.hasText(condition.keyword())) {
                Expression<Double> match = cb.function(
                    "match",
                    Double.class,
                    root.get("name"),
                    cb.literal(condition.keyword() + "*"),
                    cb.literal("IN BOOLEAN MODE")
                );

                // match 결과가 0보다 크면 검색어 포함으로 간주
                predicates.add(cb.greaterThan(match, 0.0));
            }

            // 2. 브랜드 필터 (성능 핵심: brand 객체 전체 조인 대신 ID 직접 비교)
            if (condition.brandIds() != null && !condition.brandIds().isEmpty()) {
                predicates.add(root.get("brand").get("id").in(condition.brandIds()));
            }

            // 3. 품절 제외 필터
            if (Boolean.TRUE.equals(condition.excludeSoldOut())) {
                predicates.add(cb.equal(root.get("productStatus"), ProductStatus.SALE));
            }

            // 중복 발생 여부 체크를 위한 플래그
            boolean needsDistinct = false;

            // 4. 카테고리 필터 (INNER JOIN으로 변경하여 필터링 속도 향상)
            if (condition.categoryIds() != null && !condition.categoryIds().isEmpty()) {
                root.join("productCategories", JoinType.INNER)
                    .get("category").get("id").in(condition.categoryIds());
                needsDistinct = true;
            }

            // 5. 배송/포장 타입 필터
            if (addOptionFilter(predicates, root, cb, "delivery", condition.deliveryTypes())) {
                needsDistinct = true;
            }
            if (addOptionFilter(predicates, root, cb, "packaging", condition.packagingTypes())) {
                needsDistinct = true;
            }

            // 6. JOIN이 발생했을 때만 distinct 적용 (CPU 부하 감소 핵심)
            if (needsDistinct) {
                query.distinct(true);
            } else {
                query.distinct(false);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 옵션 필터링 헬퍼 메서드
     * @return 조인 발생 여부 (distinct 설정용)
     */
    private static boolean addOptionFilter(List<Predicate> predicates,
                                           Root<Product> root,
                                           CriteriaBuilder cb,
                                           String type,
                                           List<String> values) {
        if (values != null && !values.isEmpty()) {
            // 필터링 용도는 INNER JOIN이 훨씬 빠릅니다.
            Join<Object, Object> optionJoin = root.join("productDetails", JoinType.INNER)
                .join("optionMappings", JoinType.INNER)
                .join("productOption", JoinType.INNER);

            Predicate typeMatch = cb.equal(optionJoin.get("optionType"), type);
            Predicate valueMatch = optionJoin.get("optionValue").in(values);

            predicates.add(cb.and(typeMatch, valueMatch));
            return true;
        }
        return false;
    }
}

