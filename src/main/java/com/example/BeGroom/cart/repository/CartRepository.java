package com.example.BeGroom.cart.repository;

import com.example.BeGroom.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // 회원 ID로 장바구니 조회
    @Query("select c from Cart c left join fetch c.cartItems where c.member.id = :memberId")
    Optional<Cart> findByMemberId(@Param("memberId") Long memberId);
}
