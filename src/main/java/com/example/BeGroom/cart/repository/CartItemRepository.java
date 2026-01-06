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

    // 장바구니 ID로 모든 상품 조회 (최근 담긴순)
    List<CartItem> findByCart_CartIdOrderByCreatedAtDesc(Long cartId);

    // 장바구니 ID와 선택 상태로 상품 조회
    List<CartItem> findByCart_CartIdAndIsSelected(Long cartId, Boolean isSelected);

    // 장바구니 ID와 상품 상세 ID로 조회 - 상품이 이미 담겨있는지 확인하는 용도
    Optional<CartItem> findByCart_CartIdAndProductDetail_ProductDetailId(Long cartId, Long productDetailId);

    // 장바구니 ID로 모든 상품 삭제
    void deleteByCart_CartId(Long cartId);

    // 장바구니의 상품 개수 조회
    long countByCart_CartId(Long cartId);

    // 선택된 상품 개수 조회
    long countByCart_CartIdAndIsSelected(Long cartId, boolean isSelected);

    // 장바구니의 모든 상품 선택 상태 변경 (전체 선택/선택 해제)
    @Modifying
    @Query("""
        UPDATE CartItem ci
        SET ci.isSelected = :isSelected
        WHERE ci.cart.cartId = :cartId
    """)
    int updateAllSelectedByCartId(
            @Param("cartId") Long cartId,
            @Param("isSelected") Boolean isSelected
    );

    // 선택된 상품 모두 삭제
    void deleteByCart_CartIdAndIsSelected(Long cartId, Boolean isSelected);
}
