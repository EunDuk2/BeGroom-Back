package com.example.BeGroom.wallet.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.wallet.domain.Wallet;
import com.example.BeGroom.wallet.dto.WalletTransactionPageResDto;
import com.example.BeGroom.wallet.repository.WalletRepository;
import com.example.BeGroom.wallet.repository.WalletTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;

    @Override
    public Page<WalletTransactionPageResDto> getWalletTransactionPage(Long memberId, Pageable pageable) {
        // 사용자 검증
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        // 지갑 조회
        Wallet wallet = walletRepository.findByMember(member).orElseThrow(() -> new EntityNotFoundException("없는 wallet입니다."));
        // 결과 반환
        return walletTransactionRepository.findByWalletOrderByCreatedAtDesc(wallet, pageable)
                    .map(WalletTransactionPageResDto::fromEntity);
    }
}
