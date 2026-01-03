package com.example.BeGroom.product.specification;



import com.example.BeGroom.product.domain.Product;

import com.example.BeGroom.product.dto.ProductSearchCondition;

import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Component;



import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;

import java.util.List;



@Component

public class ProductSpecification {



// 검색 조건에 따른 Specification 생성

    public static Specification<Product> searchByCondition(

            ProductSearchCondition condition,

            List<Long> productIdsWithCategory,

            List<Long> productIdsWithDelivery,

            List<Long> productIdsWithPackaging

    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();



            predicates.add(root.get("productStatus").in(

                    List.of(Product.ProductStatus.SALE, Product.ProductStatus.SOLD_OUT)

            ));



            if (condition.getKeyword() != null && !condition.getKeyword().trim().isEmpty()) {

                predicates.add(criteriaBuilder.like(

                        root.get("name"),

                        "%" + condition.getKeyword() + "%"

                ));

            }



            if (productIdsWithCategory != null && !productIdsWithCategory.isEmpty()) {

                predicates.add(root.get("productId").in(productIdsWithCategory));

            }



            if (condition.getBrandIds() != null && !condition.getBrandIds().isEmpty()) {

                predicates.add(root.get("brandId").in(condition.getBrandIds()));

            }



            if (productIdsWithDelivery != null && !productIdsWithDelivery.isEmpty()) {

                predicates.add(root.get("productId").in(productIdsWithDelivery));

            }



            if (productIdsWithPackaging != null && !productIdsWithPackaging.isEmpty()) {

                predicates.add(root.get("productId").in(productIdsWithPackaging));

            }



            if (condition.getExcludeSoldOut() != null && condition.getExcludeSoldOut()) {

                predicates.add(criteriaBuilder.equal(root.get("isSoldOut"), false));

            }



            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));

        };

    }

}

