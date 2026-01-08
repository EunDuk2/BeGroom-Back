package com.example.BeGroom.wishlist.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductImage;
import com.example.BeGroom.product.repository.ProductImageRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.wishlist.domain.Wishlist;
import com.example.BeGroom.wishlist.dto.WishlistResDto;
import com.example.BeGroom.wishlist.repository.WishlistRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    // 위시리스트 전체 조회 (DTO 변환)
    public List<WishlistResDto> getWishlist(Long memberId, Sort sort) {
        List<Wishlist> wishlists = wishlistRepository.findByMember_Id(memberId, sort);

        return wishlists.stream()
                .map(this::convertToDto)
                .toList();
    }

    // 위시리스트 토글 (추가/삭제)
    @Transactional
    public void toggleWishlist(Long memberId, Long productId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        Optional<Wishlist> existingWishlist = wishlistRepository.findByMember_IdAndProduct_ProductId(memberId, productId);

        if (existingWishlist.isPresent()) {
            // 이미 있으면 삭제
            wishlistRepository.delete(existingWishlist.get());
            product.decreaseWishlistCount(); // 여기서만 감소
        } else {
            // 없으면 추가
            Wishlist wishlist = Wishlist.create(member, product);
            wishlistRepository.save(wishlist);
            product.increaseWishlistCount(); // 여기서만 증가
        }
    }

    // 위시리스트 상품 개수 조회
    public long getWishlistCount(Long memberId) {
        return wishlistRepository.countByMember_Id(memberId);
    }

    // 상품별 찜한 총 회원 수 조회
    public long getProductWishCount(Long productId) {
        return wishlistRepository.countByProduct_ProductId(productId);
    }

    // ===== Private 헬퍼 메서드 =====
    // Wishlist -> WishlistResDto 변환
    private WishlistResDto convertToDto(Wishlist wishlist) {
        Product product = wishlist.getProduct();

        // 메인 이미지
        String mainImageUrl = productImageRepository
                .findByProduct_ProductIdAndImageTypeOrderBySortOrderAsc(
                        product.getProductId(),
                        ProductImage.ImageType.MAIN
                )
                .stream()
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return WishlistResDto.of(
                wishlist,
                product.getName(),
                mainImageUrl,
                product.getSalesPrice(),
                product.getDiscountedPrice(),
                product.getDiscountRate()
        );
    }
}
