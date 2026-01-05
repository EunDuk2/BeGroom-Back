package com.example.BeGroom.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductAggregate {

    private Long orderId;
    private String representativeProductName;
    private Long totalQuantity;
    private Long productCount;

}
