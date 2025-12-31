package com.example.BeGroom.seller.service;

import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.DashboardResDto;
import com.example.BeGroom.seller.dto.SellerCreateReqDto;
import com.example.BeGroom.seller.repository.SellerRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService{

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
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

    // 대시보드 조회
    @Override
    public DashboardResDto getDashboard(Long sellerId){
        // 판매자 주문 수
        int orderCnt = 0;
        // 판매자 상품 수
        int productCnt = 0;
        // 판매자 매출(환불 제외)
        long salesAmount = 0L;
        // 주문 없는 경우

        return new DashboardResDto(
                orderCnt,
                productCnt,
                salesAmount
        );
    }

}
