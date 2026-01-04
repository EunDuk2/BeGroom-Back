package com.example.BeGroom.seller.service;

import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.payment.domain.Payment;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.res.DashboardResDto;
import com.example.BeGroom.seller.dto.res.OrderManageResDto;
import com.example.BeGroom.seller.dto.req.SellerCreateReqDto;
import com.example.BeGroom.seller.dto.res.RecentActivityResDto;
import com.example.BeGroom.seller.repository.SellerRepository;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerServiceImpl implements SellerService{

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SettlementRepository settlementRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    @Override
    public Seller create(SellerCreateReqDto sellerCreateReqDto) {
        if(sellerRepository.findByEmail(sellerCreateReqDto.getEmail()).isPresent()) {
            throw new EntityExistsException("이미 존재하는 판매자입니다.");
        }

        Seller seller = Seller.createSeller(
                sellerCreateReqDto.getEmail(),
                sellerCreateReqDto.getName(),
                passwordEncoder.encode(sellerCreateReqDto.getPassword()),
                sellerCreateReqDto.getPhoneNumber()
        );

        sellerRepository.save(seller);

        return seller;
    }

    // 대시보드 요약 정보 조회
    @Override
    public DashboardResDto getDashboard(Long sellerId){
        // 총 주문 수
        int orderCnt = orderRepository.countCompletedOrdersBySeller(sellerId);
        // 총 상품 수
        int productCnt = productRepository.countBySellerIdAndDeletedAtIsNull(sellerId);
        // 총 매출(환불 제외)
        long salesAmount = settlementRepository.sumSalesAmountBySeller(sellerId);
        // 주문 없는 경우

        return new DashboardResDto(
                orderCnt,
                productCnt,
                salesAmount
        );
    }

    // 주문관리 조회
    @Override
    public OrderManageResDto getOrderManage(Long sellerId){
        // 총 환불 및 미정산 수
        OrderManageResDto.Summary summary =
                new OrderManageResDto.Summary(1, 2);
        // 주문 상품 목록
        List<OrderManageResDto.OrderItem> orders = List.of();

        return new OrderManageResDto(summary, orders);
    }

    // 최근 활동 조회
    @Override
    RecentActivityResDto getRecentActivities(Long sellerId){
        RecentActivityResDto.RecentOrderDto recentOrderDto = orderRepository.findLatestOrderBySeller(sellerId);
        RecentActivityResDto.RecentRefundDto recentRefundDto = paymentRepository.findLatestRefundBySeller(sellerId);
        RecentActivityResDto.RecentSettlementDto recentSettlementDto = settlementRepository.findLatestSettledBySeller(sellerId);

        return new RecentActivityResDto(
                recentOrderDto,
                recentRefundDto,
                recentSettlementDto
        );
    }
}
