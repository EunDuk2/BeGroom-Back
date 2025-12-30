package com.example.BeGroom.product.dto.crawling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * API 상세 응답 DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailResponse {
    private DetailData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailData {

        @JsonProperty("no")
        private Long no;

        @JsonProperty("name")
        private String name;

        @JsonProperty("deal_products")
        private List<DealProduct> dealProducts;
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
    }
}
