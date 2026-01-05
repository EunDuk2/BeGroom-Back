package com.example.BeGroom.pointCharge.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.pointCharge.domain.ChargeStatus;
import com.example.BeGroom.pointCharge.domain.PointCharge;
import com.example.BeGroom.pointCharge.dto.PointChargeReqDto;
import com.example.BeGroom.pointCharge.dto.PointChargeResDto;
import com.example.BeGroom.pointCharge.repository.PointChargeRepository;
import com.example.BeGroom.wallet.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointChargeServiceImpl implements PointChargeService {

    private final PointChargeRepository pointChargeRepository;
    private final MemberRepository memberRepository;
    private final WalletService walletService;

    @Override
    @Transactional
    public PointChargeResDto pointCharge(Long memberId, PointChargeReqDto reqDto) {
        // 사용자 조회 및 검증
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));

        // amount 검증
        if (reqDto.getAmount() <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        // pointCharge 생성 및 Request
        PointCharge pointCharge = PointCharge.create(member, reqDto.getAmount(), ChargeStatus.REQUESTED);
        pointChargeRepository.save(pointCharge); // 영속

        // wallet의 포인트 충전 비즈니스 로직 사용
        walletService.chargePoint(memberId, reqDto.getAmount(), pointCharge.getId());

        // pointCharge Completed
        pointCharge.completeCharge();

        return PointChargeResDto.builder()
                .pointChargeId(pointCharge.getId())
                .balance(walletService.getBalance(memberId))
                .build();
    }
}
