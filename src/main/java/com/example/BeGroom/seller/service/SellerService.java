package com.example.BeGroom.seller.service;

import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.res.DashboardResDto;
import com.example.BeGroom.seller.dto.res.OrderInfoResDto;
import com.example.BeGroom.seller.dto.res.OrderListResDto;
import com.example.BeGroom.seller.dto.req.SellerCreateReqDto;
import com.example.BeGroom.seller.dto.res.RecentActivityResDto;
import org.springframework.data.domain.Page;

public interface SellerService {

    // 회원가입
    Seller create(SellerCreateReqDto sellerCreateReqDto);

    // 대시보드 조회
    DashboardResDto getDashboard(Long sellerId);

    // 주문 관리 조회
    OrderInfoResDto getOrderInfo(Long sellerId);

    // 주문 목록 조회
    Page<OrderListResDto> getOrderList(Long sellerId, int page);

    // 최근 활동 조회
    RecentActivityResDto getRecentActivities(Long sellerId);
}
