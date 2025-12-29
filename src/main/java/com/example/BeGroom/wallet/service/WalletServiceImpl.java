package com.example.BeGroom.wallet.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.wallet.domain.Wallet;
import com.example.BeGroom.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;


    @Override
    @Transactional
    public Wallet create(Member member) {
        // 존재 유무 검증
        if(walletRepository.findByMember(member).isPresent()) {
            throw new IllegalStateException("존재하는 Wallet 입니다.");
        }
        // wallet 생성
        Wallet wallet = Wallet.create(member);
        walletRepository.save(wallet);

        return wallet;
    }
}
