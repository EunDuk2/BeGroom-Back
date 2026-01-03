package com.example.BeGroom.product.dto.crawling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 상품 기본 정보 API 응답 DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawlingResponse {

    private List<ProductData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductData {

        @JsonProperty("no")
        private Long no;

        @JsonProperty("name")
        private String name;

        @JsonProperty("short_description")
        private String shortDescription;

        @JsonProperty("list_image_url")
        private String listImageUrl;

        @JsonProperty("sales_price")
        private Integer salesPrice;

        @JsonProperty("discounted_price")
        private Integer discountedPrice;

        @JsonProperty("discount_rate")
        private Integer discountRate;

        @JsonProperty("is_buy_now")
        private Boolean isBuyNow;

        @JsonProperty("is_purchase_status")
        private Boolean isPurchaseStatus;

        @JsonProperty("is_only_adult")
        private Boolean isOnlyAdult;

        @JsonProperty("is_sold_out")
        private Boolean isSoldOut;

        @JsonProperty("sold_out_title")
        private String soldOutTitle;

        @JsonProperty("sold_out_text")
        private String soldOutText;

        @JsonProperty("can_restock_notify")
        private Boolean canRestockNotify;

        @JsonProperty("is_low_stock")
        private Boolean isLowStock;

        @JsonProperty("is_multiple_price")
        private Boolean isMultiplePrice;

        /**
         * null 안전 처리
         */
        public Boolean getIsSoldOut() {
            return isSoldOut != null ? isSoldOut : false;
        }

        public Boolean getIsBuyNow() {
            return isBuyNow != null ? isBuyNow : false;
        }

        public Boolean getIsPurchaseStatus() {
            return isPurchaseStatus != null ? isPurchaseStatus : false;
        }

        public Boolean getIsOnlyAdult() {
            return isOnlyAdult != null ? isOnlyAdult : false;
        }

        public Boolean getIsLowStock() {
            return isLowStock != null ? isLowStock : false;
        }

        public Boolean getCanRestockNotify() {
            return canRestockNotify != null ? canRestockNotify : false;
        }

        public Integer getDiscountRate() {
            return discountRate != null ? discountRate : 0;
        }

        public Integer getStock() {
            if (getIsSoldOut()) {
                return 0;
            }
            if (getIsLowStock()) {
                return 50;
            }
            return 999;
        }
    }
}
