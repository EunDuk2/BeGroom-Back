package com.example.BeGroom.wallet.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.wallet.domain.ReferenceType;
import com.example.BeGroom.wallet.domain.TransactionType;
import com.example.BeGroom.wallet.domain.Wallet;
import com.example.BeGroom.wallet.domain.WalletTransaction;
import com.example.BeGroom.wallet.repository.WalletRepository;
import com.example.BeGroom.wallet.repository.WalletTransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final MemberRepository memberRepository;
    private final WalletTransactionRepository walletTransactionRepository;


    @Override
    @Transactional
    public Wallet create(Member member) {
        // 존재 유무 검증
        if(walletRepository.findByMember(member).isPresent()) {
            throw new IllegalStateException("존재하는 wallet입니다.");
        }
        // wallet 생성
        Wallet wallet = Wallet.create(member);
        walletRepository.save(wallet);

        return wallet;
    }

    @Override
    @Transactional
    public void chargePoint(Long memberId, Long amount, Long referenceId) {
        // 사용자 검증
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        // 지갑 조회
        Wallet wallet = walletRepository.findByMember(member).orElseThrow(() -> new EntityNotFoundException("없는 wallet입니다."));
        Long balanceBefore = wallet.getBalance(); // 충전 전 잔액
        // 잔액 충전
        wallet.increaseBalance(amount);
        Long balanceAfter = wallet.getBalance(); // 충전 후 잔액
        // 원장 생성
        WalletTransaction walletTransaction
                = WalletTransaction.create(wallet, TransactionType.CHARGE, balanceBefore, amount, balanceAfter, ReferenceType.CHARGE, referenceId);
        // 원장 저장
        walletTransactionRepository.save(walletTransaction);
    }

    @Override
    @Transactional
    public void payPoint(Long memberId, Long amount, Long referenceId) {
        // 사용자 검증
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        // 지갑 조회
        Wallet wallet = walletRepository.findByMember(member).orElseThrow(() -> new EntityNotFoundException("없는 wallet입니다."));
        Long balanceBefore = wallet.getBalance();
        // 결제 금액 차감
        wallet.decreaseBalance(amount);
        Long balanceAfter = wallet.getBalance();
        // 원장 생성
        WalletTransaction walletTransaction
                = WalletTransaction.create(wallet, TransactionType.PAYMENT, balanceBefore, amount, balanceAfter, ReferenceType.ORDER, referenceId);
        // 원장 저장
        walletTransactionRepository.save(walletTransaction);
    }

    @Override
    @Transactional
    public void refundPoint(Long memberId, Long amount, Long referenceId) {
        // 사용자 검증
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        // 지갑 조회
        Wallet wallet = walletRepository.findByMember(member).orElseThrow(() -> new EntityNotFoundException("없는 wallet입니다."));
        Long balanceBefore = wallet.getBalance();
        // 환불 금액 증가
        wallet.increaseBalance(amount);
        Long balanceAfter = wallet.getBalance();
        // 원장 생성
        WalletTransaction walletTransaction
                = WalletTransaction.create(wallet, TransactionType.REFUND, balanceBefore, amount, balanceAfter, ReferenceType.ORDER, referenceId);
        // 원장 저장
        walletTransactionRepository.save(walletTransaction);
    }

}
