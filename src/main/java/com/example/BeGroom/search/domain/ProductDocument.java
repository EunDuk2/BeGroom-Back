//package com.example.BeGroom.search.domain;
//
//import com.example.BeGroom.product.domain.Product;
//import jakarta.persistence.Id;
//import lombok.AccessLevel;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import org.springframework.data.elasticsearch.annotations.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Document(indexName = "products")
//@Setting(settingPath = "elasticsearch/product-settings.json")
//public class ProductDocument {
//
//    @Id
//    private String id;
//
//    @Field(type = FieldType.Long)
//    private Long productNo;
//
//    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
//    private String name;
//
//    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
//    private String shortDescription;
//
//    @Field(type = FieldType.Integer)
//    private Integer salesPrice;
//
//    @Field(type = FieldType.Integer)
//    private Integer discountedPrice;
//
//    @Field(type = FieldType.Integer)
//    private Integer discountRate;
//
//    @Field(type = FieldType.Long)
//    private Long brandId;
//
//    @Field(type = FieldType.Keyword)
//    private String brandName;
//
//    @Field(type = FieldType.Long)
//    private List<Long> categoryIds;
//
//    @Field(type = FieldType.Keyword)
//    private List<String> categoryNames;
//
//    @Field(type = FieldType.Keyword)
//    private List<String> deliveryTypes;
//
//    @Field(type = FieldType.Keyword)
//    private List<String> packagingTypes;
//
//    @Field(type = FieldType.Keyword)
//    private String productStatus;
//
//    @Field(type = FieldType.Boolean)
//    private Boolean isSoldOut;
//
//    @Field(type = FieldType.Keyword)
//    private String mainImageUrl;
//
//    @Field(type = FieldType.Integer)
//    private Integer wishlistCount;
//
//    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
//    private LocalDateTime createdAt;
//
//    @Builder
//    public ProductDocument(String id, Long productNo, String name, String shortDescription,
//                           Integer salesPrice, Integer discountedPrice, Integer discountRate,
//                           Long brandId, String brandName,
//                           List<Long> categoryIds, List<String> categoryNames,
//                           List<String> deliveryTypes, List<String> packagingTypes,
//                           String productStatus, Boolean isSoldOut, String mainImageUrl, Integer wishlistCount, LocalDateTime createdAt) {
//        this.id = id;
//        this.productNo = productNo;
//        this.name = name;
//        this.shortDescription = shortDescription;
//        this.salesPrice = salesPrice;
//        this.discountedPrice = discountedPrice;
//        this.discountRate = discountRate;
//        this.brandId = brandId;
//        this.brandName = brandName;
//        this.categoryIds = categoryIds;
//        this.categoryNames = categoryNames;
//        this.deliveryTypes = deliveryTypes;
//        this.packagingTypes = packagingTypes;
//        this.productStatus = productStatus;
//        this.isSoldOut = isSoldOut;
//        this.mainImageUrl = mainImageUrl;
//        this.wishlistCount = wishlistCount;
//        this.createdAt = createdAt;
//    }
//
//    public static ProductDocument from(Product product) {
//        return ProductDocument.builder()
//            .id(String.valueOf(product.getId()))
//            .productNo(product.getNo())
//            .name(product.getName())
//            .shortDescription(product.getShortDescription())
//            .salesPrice(product.getSalesPrice())
//            .discountedPrice(product.getDiscountedPrice())
//            .discountRate(product.getDiscountRate())
//            .brandId(product.getBrand().getId())
//            .brandName(product.getBrand().getName())
//            .categoryIds(product.getCategoryIdsForSearch())
//            .categoryNames(product.getCategoryNamesForSearch())
//            .deliveryTypes(product.getDeliveryTypesForSearch())
//            .packagingTypes(product.getPackagingTypesForSearch())
//            .productStatus(product.getProductStatus().name())
//            .isSoldOut(product.isSoldOut())
//            .mainImageUrl(product.getMainImageUrl())
//            .wishlistCount(product.getWishlistCount())
//            .createdAt(product.getCreatedAt())
//            .build();
//    }
//}
