package com.example.BeGroom.pointCharge.service;

import com.example.BeGroom.pointCharge.domain.PointCharge;
import com.example.BeGroom.pointCharge.dto.PointChargeReqDto;
import com.example.BeGroom.pointCharge.dto.PointChargeResDto;

public interface PointChargeService {
    PointChargeResDto pointCharge(Long memberId, PointChargeReqDto reqDto);
}
