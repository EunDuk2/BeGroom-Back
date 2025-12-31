package com.example.BeGroom.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateReqDto {
    private List<OrderProductReqDto> orderProductList;
}
