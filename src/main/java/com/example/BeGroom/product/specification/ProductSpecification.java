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

            predicates.add(root.get("productStatus").in(ProductStatus.SALE, ProductStatus.SOLD_OUT));

            // 1. 키워드 검색 (공백 제거 검색 포함)
            if (StringUtils.hasText(condition.getKeyword())) {
                String cleanKeyword = condition.getKeyword().replaceAll("\\s+", "");
                predicates.add(cb.like(
                    cb.function("replace", String.class, root.get("name"), cb.literal(" "), cb.literal("")),
                    "%" + cleanKeyword + "%"
                ));
            }

            // 2. 브랜드 필터
            if (condition.getBrandIds() != null && !condition.getBrandIds().isEmpty()) {
                predicates.add(root.get("brand").get("id").in(condition.getBrandIds()));
            }

            // 3. 품절 제외 필터
            if (Boolean.TRUE.equals(condition.getExcludeSoldOut())) {
                predicates.add(cb.equal(root.get("productStatus"), ProductStatus.SALE));
            }

            // 4. 카테고리 필터 (다대다 연관관계를 통한 조인 필터링)
            if (condition.getCategoryIds() != null && !condition.getCategoryIds().isEmpty()) {
                predicates.add(root.join("productCategories", JoinType.LEFT)
                    .get("category").get("id").in(condition.getCategoryIds()));
            }

            // 5. 배송/포장 타입 필터
            addOptionFilter(predicates, root, cb, "delivery", condition.getDeliveryTypes());
            addOptionFilter(predicates, root, cb, "packaging", condition.getPackagingTypes());

            // 6. 삭제 상품 제외
            predicates.add(cb.isNull(root.get("deletedAt")));

            // JOIN으로 인한 중복 결과 방지
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 옵션 필터링 헬퍼 메서드 (중복 코드 방지)
     */
    private static void addOptionFilter(List<Predicate> predicates,
                                        Root<Product> root,
                                        CriteriaBuilder cb,
                                        String type,
                                        List<String> values) {
        if (values != null && !values.isEmpty()) {
            // 다중 조인 경로 설정
            Join<Object, Object> optionJoin = root.join("productDetails", JoinType.LEFT)
                .join("productOptionMappings", JoinType.LEFT)
                .join("productOption", JoinType.LEFT);

            // 해당 옵션 타입과 값들이 일치하는지 확인
            Predicate typeMatch = cb.equal(optionJoin.get("optionType"), type);
            Predicate valueMatch = optionJoin.get("optionValue").in(values);

            // 두 조건을 AND로 묶어 추가
            predicates.add(cb.and(typeMatch, valueMatch));
        }
    }
}

