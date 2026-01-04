package com.example.BeGroom.wishlist.repository;

import com.example.BeGroom.wishlist.domain.Wishlist;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // 회원의 위시리스트 전체 조회
    List<Wishlist> findByMemberId(Long memberId, Sort sort);

    // 회원이 특정 상품을 이미 찜했는지 확인 (단건 조회)
    Optional<Wishlist> findByMemberIdAndProductId(Long memberId, Long productId);

    // 회원의 특정 상품 위시 여부 확인
    boolean existsByMemberIdAndProductId(Long memberId, Long productId);

    // 특정 회원의 특정 상품 찜 취소 (삭제)
    void deleteByMemberIdAndProductId(Long memberId, Long productId);

    // 회원의 위시리스트 개수 조회
    long countByMemberId(Long memberId);

    // 특정 상품을 찜한 회원 수 조회
    long countByProductId(Long productId);
}
