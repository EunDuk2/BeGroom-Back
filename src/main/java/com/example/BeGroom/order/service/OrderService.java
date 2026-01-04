package com.example.BeGroom.order.service;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.dto.OrderCreateReqDto;
import com.example.BeGroom.order.dto.OrderDetailResDto;
import com.example.BeGroom.order.dto.OrderProductReqDto;
import com.example.BeGroom.order.dto.OrderSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Order create(Long memberId, OrderCreateReqDto reqDto);
    Page<OrderSummaryDto> getOrders(Pageable pageable, Long memberId);
    OrderDetailResDto getOrderDetail(Long orderId);
}
