package com.example.BeGroom.pointCharge.service;

import com.example.BeGroom.pointCharge.domain.PointCharge;
import com.example.BeGroom.pointCharge.dto.PointChargeReqDto;

public interface PointChargeService {
    PointCharge pointCharge(Long memberId, PointChargeReqDto reqDto);
}
