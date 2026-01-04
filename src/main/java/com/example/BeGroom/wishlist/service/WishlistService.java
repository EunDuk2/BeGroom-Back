package com.example.BeGroom.wishlist.service;

import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductImage;
import com.example.BeGroom.product.repository.ProductImageRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.wishlist.domain.Wishlist;
import com.example.BeGroom.wishlist.dto.WishlistResDto;
import com.example.BeGroom.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    // 위시리스트 전체 조회 (DTO 변환)
    public List<WishlistResDto> getWishlist(Long memberId, Sort sort) {
        List<Wishlist> wishlists = wishlistRepository.findByMemberId(memberId, sort);

        return wishlists.stream()
                .map(this::convertToDto)
                .toList();
    }

    // 위시리스트 토글 (추가/삭제)
    @Transactional
    public void toggleWishlist(Long memberId, Long productId) {
        wishlistRepository.findByMemberIdAndProductId(memberId, productId)
                .ifPresentOrElse(
                        wishlist -> {
                            wishlistRepository.deleteByMemberIdAndProductId(memberId, productId);
                            log.info("위시리스트 삭제 - memberId: {}, productId: {}", memberId, productId);
                        },
                        () -> {
                            Wishlist wishlist = Wishlist.builder()
                                    .memberId(memberId)
                                    .productId(productId)
                                    .build();
                            wishlistRepository.save(wishlist);
                            log.info("위시리스트 추가 - memberId: {}, productId: {}", memberId, productId);
                        }
                );
    }

    // 위시리스트 상품 개수 조회
    public long getWishlistCount(Long memberId) {
        return wishlistRepository.countByMemberId(memberId);
    }

    // 상품별 찜한 총 회원 수 조회
    public long getProductWishCount(Long productId) {
        return wishlistRepository.countByProductId(productId);
    }

    // ===== Private 헬퍼 메서드 =====
    // Wishlist -> WishlistResDto 변환
    private WishlistResDto convertToDto(Wishlist wishlist) {
        Product product = productRepository.findById(wishlist.getProductId())
                .orElse(null);

        if (product == null) {
            log.warn("상품을 찾을 수 없음 - productId: {}", wishlist.getProductId());
            return WishlistResDto.from(wishlist);
        }

        // 메인 이미지
        String mainImageUrl = productImageRepository
                .findByProductIdAndImageTypeOrderBySortOrderAsc(
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
