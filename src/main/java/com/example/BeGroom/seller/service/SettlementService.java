package com.example.BeGroom.seller.service;

import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.req.SellerCreateReqDto;
import com.example.BeGroom.seller.dto.res.SettlementManageResDto;
import jakarta.validation.Valid;

public interface SettlementService {

    // 정산관리 조회
    SettlementManageResDto getSettlementManage(Long sellerId);
}
