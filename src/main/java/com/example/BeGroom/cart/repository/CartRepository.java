package com.example.BeGroom.cart.repository;

import com.example.BeGroom.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // 회원 ID로 장바구니 조회
    Optional<Cart> findByMemberId(Long memberId);
}
