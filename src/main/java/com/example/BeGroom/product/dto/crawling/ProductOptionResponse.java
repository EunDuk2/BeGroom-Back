package com.example.BeGroom.product.dto.crawling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductOptionResponse {

    private OptionData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OptionData {

        @JsonProperty("no")
        private Long no;

        @JsonProperty("storage_type")
        private List<String> storageType;

        @JsonProperty("delivery_type_infos")
        private List<DeliveryTypeInfo> deliveryTypeInfos;

        @JsonProperty("expiration_date")
        private String expirationDate;

        @JsonProperty("guides")
        private List<String> guides;

        @JsonProperty("product_detail")
        private ProductDetailInfo productDetail;

        @JsonProperty("product_notice")
        private List<ProductNotice> productNotice;

        @JsonProperty("deal_products")
        private List<DealProduct> dealProducts;

        @JsonProperty("brand_info")
        private BrandInfo brandInfo;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductDetailInfo {

        @JsonProperty("legacy_content")
        private String legacyContent;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductNotice {

        @JsonProperty("notices")
        private List<Notice> notices;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Notice {

        @JsonProperty("title")
        private String title;

        @JsonProperty("description")
        private String description;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeliveryTypeInfo {

        @JsonProperty("type")
        private String type;

        @JsonProperty("description")
        private String description;

        @JsonProperty("guide")
        private String guide;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DealProduct {

        @JsonProperty("no")
        private Long no;

        @JsonProperty("name")
        private String name;

        @JsonProperty("base_price")
        private Integer basePrice;

        @JsonProperty("discounted_price")
        private Integer discountedPrice;

        @JsonProperty("is_sold_out")
        private Boolean isSoldOut;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BrandInfo {

        @JsonProperty("name_gate")
        private NameGate nameGate;

        @JsonProperty("logo_gate")
        private LogoGate logoGate;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NameGate {

        @JsonProperty("is_shown")
        private Boolean isShown;

        @JsonProperty("brand_code")
        private Long brandCode;

        @JsonProperty("name")
        private String name;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LogoGate {

        @JsonProperty("is_shown")
        private Boolean isShown;

        @JsonProperty("brand_code")
        private Long brandCode;

        @JsonProperty("title")
        private String title;

        @JsonProperty("image")
        private String image;

        @JsonProperty("description")
        private String description;
    }
}