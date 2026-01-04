package com.example.BeGroom.cart.repository;

import com.example.BeGroom.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 장바구니 ID로 모든 상품 조회
    List<CartItem> findByCartId(Long cartId);

    // 장바구니 ID와 선택 상태로 상품 조회
    List<CartItem> findByCartIdAndIsSelected(Long cartId, Boolean isSelected);

    // 장바구니 ID와 상품 상세 ID로 조회 - 상품이 이미 담겨있는지 확인하는 용도
    Optional<CartItem> findByCartIdAndProductDetailId(Long cartId, Long productDetailId);

    // 장바구니 ID로 모든 상품 삭제
    void deleteByCartId(Long cartId);

    // 장바구니의 상품 개수 조회
    long countByCartId(Long cartId);

    // 선택된 상품 개수 조회
    long countByCartIdAndIsSelected(Long cartId, boolean isSelected);

    // 장바구니의 모든 상품 선택 상태 변경 (전체 선택/선택 해제)
    @Modifying
    @Query("""
        UPDATE CartItem ci
        SET ci.isSelected = :isSelected
        WHERE ci.cartId = :cartId
    """)
    int updateAllSelectedByCartId(
            @Param("cartId") Long cartId,
            @Param("isSelected") Boolean isSelected
    );

    // 선택된 상품 모두 삭제
    void deleteByCartIdAndIsSelected(Long cartId, Boolean isSelected);
}
