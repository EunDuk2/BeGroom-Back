package com.example.BeGroom.seller.service;

import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.res.DashboardResDto;
import com.example.BeGroom.seller.dto.res.OrderManageResDto;
import com.example.BeGroom.seller.dto.req.SellerCreateReqDto;
import com.example.BeGroom.seller.dto.res.RecentActivityResDto;

public interface SellerService {

    // 회원가입
    Seller create(SellerCreateReqDto sellerCreateReqDto);

    // 대시보드 조회
    DashboardResDto getDashboard(Long sellerId);

    // 주문관리 조회
    OrderManageResDto getOrderManage(Long sellerId);

    // 최근 활동 조회
    RecentActivityResDto getRecentActivities(Long sellerId);
}
