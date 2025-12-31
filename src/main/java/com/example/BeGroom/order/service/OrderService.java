package com.example.BeGroom.order.service;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.dto.OrderCreateReqDto;
import com.example.BeGroom.order.dto.OrderProductReqDto;

import java.util.List;

public interface OrderService {
    Order create(Long memberId, OrderCreateReqDto reqDto);
}
