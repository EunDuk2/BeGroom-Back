package com.example.BeGroom.order.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.domain.OrderStatus;
import com.example.BeGroom.order.dto.*;
import com.example.BeGroom.order.repository.OrderProductRepository;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.domain.PaymentStatus;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.wallet.domain.Wallet;
import com.example.BeGroom.wallet.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;


    @Override
    @Transactional
    public Order create(Long memberId, OrderCreateReqDto reqDto) {
        // 사용자 검증
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));

        // 주문 생성
        Order order = Order.create(member, 0L, OrderStatus.CREATED);

        // 상품 리스트 조회
        List<OrderProductReqDto> orderProductReqDtoList = reqDto.getOrderProductList();
        for(OrderProductReqDto orderProductReqDto : orderProductReqDtoList) {
            Product product = productRepository.findById(orderProductReqDto.getProductId()).orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
            // 재고 검증
            product.validateOrderable(orderProductReqDto.getOrderQuantity());

            // OrderProduct 생성
            OrderProduct orderProduct = OrderProduct.create(order, product, orderProductReqDto.getOrderQuantity(), product.getSalesPrice());

            // orderProduct를 order에 추가
            order.addOrderProduct(orderProduct);
        }

        // 주문 영속
        orderRepository.save(order);

        return order;
    }

    @Override
    public Page<OrderSummaryDto> getOrders(Pageable pageable, Long memberId) {
        // 사용자 조회
        memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));

        // 주문 페이지 조회
        Page<Order> orderPage = orderRepository.findByMemberId(pageable, memberId);

        // 조회한 주문들의 id 추출
        List<Long> orderIds = orderPage.getContent()
                .stream()
                .map(Order::getId)
                .toList();

        // OrderProduct 집계 조회
        List<OrderProductAggregate> aggregates = orderProductRepository.aggregateByOrderIds(orderIds);

        // Map으로 변환
        Map<Long, OrderProductAggregate> aggregateMap = aggregates.stream()
                .collect(Collectors.toMap(OrderProductAggregate::getOrderId, a -> a));

        // dto 조합
        List<OrderSummaryDto> content = orderPage
                .map(order -> OrderSummaryDto.of(order, aggregateMap.get(order.getId()))).toList();

        // Page로 반환
        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Override
    public OrderDetailResDto getOrderDetail(Long orderId) {

        // 주문 조회
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("없는 주문입니다."));

        // 상품 목록 조회
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(orderId);

        // 결제 조회 (주문 완료일 때만)
        Payment payment = null;
        if(order.getOrderStatus() == OrderStatus.COMPLETED) {
            payment = paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.APPROVED)
                    .orElseThrow(() -> new EntityNotFoundException("없는 결제입니다."));
        } else if(order.getOrderStatus() == OrderStatus.CANCELED) {
            payment = paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.REFUNDED)
                    .orElseThrow(() -> new EntityNotFoundException("없는 결제입니다."));
        }

        return OrderDetailResDto.of(
                order,
                payment,
                orderProducts
        );
    }

    @Override
    public OrderInfoResDto getOrderInfo(Long memberId, Long orderId) {
        // 사용자 조회
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));

        // 주문 조회
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("없는 주문입니다."));

        // 지갑 조회
        Wallet wallet = walletRepository.findByMember(member).orElseThrow(() -> new EntityNotFoundException("없는 지갑입니다."));

        return OrderInfoResDto.of(member, order, wallet);
    }


}
