package com.example.BeGroom.seller.service;

import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.DashboardResDto;
import com.example.BeGroom.seller.dto.SellerCreateReqDto;

public interface SellerService {

    // 회원가입
    Seller create(SellerCreateReqDto sellerCreateReqDto);

    // 대시보드 조회
    DashboardResDto getDashboard(Long sellerId);
}
