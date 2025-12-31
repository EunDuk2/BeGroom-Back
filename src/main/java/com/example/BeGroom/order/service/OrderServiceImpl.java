package com.example.BeGroom.order.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.domain.OrderStatus;
import com.example.BeGroom.order.dto.OrderCreateReqDto;
import com.example.BeGroom.order.dto.OrderProductReqDto;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;


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
}
