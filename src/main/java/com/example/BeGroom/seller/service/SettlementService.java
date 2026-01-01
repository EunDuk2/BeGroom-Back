package com.example.BeGroom.seller.service;

import com.example.BeGroom.seller.domain.PeriodType;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.dto.req.SellerCreateReqDto;
import com.example.BeGroom.seller.dto.res.PeriodSettlementResDto;
import com.example.BeGroom.seller.dto.res.SettlementManageResDto;
import jakarta.validation.Valid;

import java.util.List;

public interface SettlementService {

    // 정산관리 조회
    SettlementManageResDto getSettlementManage(Long sellerId);
    // 기간별 정산 집계
    List<PeriodSettlementResDto> getPeriodSettlement(Long sellerId, PeriodType type);
}
