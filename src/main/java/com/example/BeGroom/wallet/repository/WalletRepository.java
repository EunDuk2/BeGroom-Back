package com.example.BeGroom.wallet.repository;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByMember(Member member);
}
