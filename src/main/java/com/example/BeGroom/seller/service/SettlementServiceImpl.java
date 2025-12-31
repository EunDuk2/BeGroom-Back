package com.example.BeGroom.seller.service;

import com.example.BeGroom.seller.dto.res.SettlementManageResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService{

    // 정산관리 조회
    @Override
    public SettlementManageResDto getSettlementManage(Long sellerId){
        // 결제금액, 환불금액, 수수료, 정산금액
        SettlementManageResDto.Summary summary =
                new SettlementManageResDto.Summary(230000, 66000, 223400, 2010600);
        // 건별 정산 리스트
        List<SettlementManageResDto.SettlementByItem> settlementByItemList = List.of();

        return new SettlementManageResDto(summary, settlementByItemList);
    }
}
