//package com.example.BeGroom.search.service;
//
//import co.elastic.clients.elasticsearch._types.FieldValue;
//import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
//import co.elastic.clients.elasticsearch._types.query_dsl.Query;
//import com.example.BeGroom.product.dto.ProductListResponse;
//import com.example.BeGroom.product.dto.ProductSearchCondition;
//import com.example.BeGroom.search.domain.ProductDocument;
//import com.example.BeGroom.search.repository.ProductSearchRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.elasticsearch.client.elc.NativeQuery;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.data.elasticsearch.core.SearchHit;
//import org.springframework.data.elasticsearch.core.SearchHits;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class ProductSearchService {
//
//    private final ProductSearchRepository productSearchRepository;
//    private final ElasticsearchOperations elasticsearchOperations;
//
//    public Page<ProductListResponse> searchProducts(ProductSearchCondition condition, Pageable pageable) {
//
//        // 1. 검색 쿼리 생성
//        Query searchQuery = buildSearchQuery(condition);
//
//        // 2. NativeQuery 생성
//        NativeQuery nativeQuery = NativeQuery.builder()
//            .withQuery(searchQuery)
//            .withPageable(pageable)
//            .build();
//
//        // 3. 검색 실행
//        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);
//
//        List<ProductListResponse> content = searchHits.getSearchHits().stream()
//            .map(SearchHit::getContent)
//            .map(this::toProductListResponse)
//            .toList();
//
//        return new PageImpl<>(
//            content,
//            pageable,
//            searchHits.getTotalHits()
//        );
//    }
//
//    private Query buildSearchQuery(ProductSearchCondition condition) {
//        return Query.of(q -> q.bool(buildBoolQuery(condition)));
//    }
//
//    private BoolQuery buildBoolQuery(ProductSearchCondition condition) {
//        return BoolQuery.of(b -> {
//            addKeywordQuery(b, condition.keyword());
//
//            addBrandFilter(b, condition.brandIds());
//            addCategoryFilter(b, condition.categoryIds());
//            addSoldOutFilter(b, condition.excludeSoldOut());
//            addDeliveryFilter(b, condition.deliveryTypes());
//            addPackagingFilter(b, condition.packagingTypes());
//            addStatusFilter(b);
//
//            return b;
//        });
//    }
//
//    private void addKeywordQuery(BoolQuery.Builder builder, String keyword) {
//        if (StringUtils.hasText(keyword)) {
//            builder.must(m -> m.match(t -> t
//                .field("name")
//                .query(keyword)
//                .analyzer("nori_analyzer")
//            ));
//        }
//    }
//
//    private void addBrandFilter(BoolQuery.Builder builder, List<Long> brandIds) {
//        if (brandIds != null && !brandIds.isEmpty()) {
//            builder.filter(f -> f.terms(t -> t
//                .field("brandId")
//                .terms(v -> v.value(brandIds.stream()
//                    .map(FieldValue::of)
//                    .toList()))
//            ));
//        }
//    }
//
//    private void addCategoryFilter(BoolQuery.Builder builder, List<Long> categoryIds) {
//        if (categoryIds != null && !categoryIds.isEmpty()) {
//            builder.filter(f -> f.terms(t -> t
//                .field("categoryIds")
//                .terms(v -> v.value(categoryIds.stream()
//                    .map(FieldValue::of)
//                    .toList()))
//            ));
//        }
//    }
//
//    private void addSoldOutFilter(BoolQuery.Builder builder, Boolean excludeSoldOut) {
//        if (Boolean.TRUE.equals(excludeSoldOut)) {
//            builder.filter(f -> f.term(t -> t
//                .field("isSoldOut")
//                .value(false)
//            ));
//        }
//    }
//
//    private void addDeliveryFilter(BoolQuery.Builder builder, List<String> deliveryTypes) {
//        if (deliveryTypes != null && !deliveryTypes.isEmpty()) {
//            builder.filter(f -> f.terms(t -> t
//                .field("deliveryTypes")
//                .terms(v -> v.value(deliveryTypes.stream()
//                    .map(FieldValue::of)
//                    .toList()))
//            ));
//        }
//    }
//
//    private void addPackagingFilter(BoolQuery.Builder builder, List<String> packagingTypes) {
//        if (packagingTypes != null && !packagingTypes.isEmpty()) {
//            builder.filter(f -> f.terms(t -> t
//                .field("packagingTypes")
//                .terms(v -> v.value(packagingTypes.stream()
//                    .map(FieldValue::of)
//                    .toList()))
//            ));
//        }
//    }
//
//    private void addStatusFilter(BoolQuery.Builder builder) {
//        builder.filter(f -> f.terms(t -> t
//            .field("productStatus")
//            .terms(v -> v.value(List.of(
//                FieldValue.of("SALE"),
//                FieldValue.of("SOLD_OUT")
//                )))
//        ));
//    }
//
//    private ProductListResponse toProductListResponse(ProductDocument document) {
//        return ProductListResponse.builder()
//            .productId(Long.valueOf(document.getId()))
//            .productNo(document.getProductNo())
//            .brand(document.getBrandName())
//            .name(document.getName())
//            .shortDescription(document.getShortDescription())
//            .salesPrice(document.getSalesPrice())
//            .discountedPrice(document.getDiscountedPrice())
//            .discountRate(document.getDiscountRate())
//            .mainImageUrl(document.getMainImageUrl())
//            .wishlistCount(document.getWishlistCount())
//            .isWishlisted(false)  // 기본값, ProductService에서 업데이트
//            .isSoldOut(document.getIsSoldOut())
//            .productStatus(document.getProductStatus())
//            .build();
//    }
//}
