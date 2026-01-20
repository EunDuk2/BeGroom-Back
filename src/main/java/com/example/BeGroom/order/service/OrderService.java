package com.example.BeGroom.order.service;

import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.dto.*;
import com.example.BeGroom.payment.domain.PaymentMethod;
import com.example.BeGroom.usecase.checkout.dto.CheckoutResDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Order create(Long memberId, OrderCreateReqDto reqDto);
    CheckoutResDto checkout(Long memberId, Long orderId, PaymentMethod paymentMethod);
    Page<OrderSummaryDto> getOrders(Pageable pageable, Long memberId);
    OrderDetailResDto getOrderDetail(Long orderId);
    OrderInfoResDto getOrderInfo(Long memberId, Long orderId);
}
