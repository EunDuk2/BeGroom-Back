package com.example.BeGroom.settlement.service;

import com.example.BeGroom.settlement.domain.PeriodType;
import com.example.BeGroom.settlement.dto.req.ProductSettlementReqDto;
import com.example.BeGroom.settlement.dto.res.PeriodSettlementResDto;
import com.example.BeGroom.settlement.dto.res.ProductSettlementResDto;
import com.example.BeGroom.settlement.dto.res.SettlementManageResDto;

import java.util.List;

public interface SettlementService {

    // 정산관리 조회
    SettlementManageResDto getSettlementManage(Long sellerId);
    // 기간별 정산 집계
    List<PeriodSettlementResDto> getPeriodSettlement(Long sellerId, PeriodType type);
    // 건별 정산 집계
    List<ProductSettlementResDto> getProductSettlement(Long sellerId, ProductSettlementReqDto productSettlementReqDto);
}
