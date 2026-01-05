package com.example.BeGroom.member.dto;

import com.example.BeGroom.order.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetMemberOrdersResDto {
    private List<OrderSummary> orders;

    @Data
    @Builder
    public static class OrderSummary {
        @JsonProperty("order_number")
        private Long orderNumber;

        @JsonProperty("total_amount")
        private Long totalAmount;

        private OrderStatus status;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonProperty("items")
        private List<OrderedItem> items;
    }

    @Data
    @Builder
    public static class OrderedItem {
        private String imageUrl;
        private String productName;
        private Integer price;
        private Integer quantity;
    }

    public static GetMemberOrdersResDto of(List<GetMemberOrdersResDto.OrderSummary> orderSummaries) {
        return new GetMemberOrdersResDto(orderSummaries);
    }
}
