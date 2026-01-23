package com.example.BeGroom.wishlist.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.wishlist.domain.Wishlist;
import com.example.BeGroom.wishlist.dto.WishlistResponse;
import com.example.BeGroom.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    // 위시리스트 전체 조회
    public List<WishlistResponse> getWishlist(Long memberId, Sort sort) {

        return wishlistRepository.findByMember_Id(memberId, sort).stream()
                .map(this::convertToDto)
                .toList();
    }

    // 위시리스트 토글 (추가/삭제)
    @Transactional
    public void toggleWishlist(Long memberId, Long productId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        wishlistRepository.findByMember_IdAndProduct_Id(memberId, productId)
            .ifPresentOrElse(
                wishlist -> {
                    wishlistRepository.delete(wishlist);
                    product.decreaseWishlistCount();
                },
                () -> {
                    Wishlist wishlist = Wishlist.create(member, product);
                    wishlistRepository.save(wishlist);
                    product.increaseWishlistCount();
                }
            );
    }

    // 위시리스트 상품 개수 조회
    public long getWishlistCount(Long memberId) {
        return wishlistRepository.countByMember_Id(memberId);
    }

    // 상품별 찜한 총 회원 수 조회
    public long getProductWishCount(Long productId) {
        return wishlistRepository.countByProduct_Id(productId);
    }

    // ===== Private 헬퍼 메서드 =====
    private WishlistResponse convertToDto(Wishlist wishlist) {
        Product product = wishlist.getProduct();

        return WishlistResponse.from(wishlist);
    }
}
