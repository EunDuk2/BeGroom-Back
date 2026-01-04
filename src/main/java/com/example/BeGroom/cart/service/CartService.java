package com.example.BeGroom.cart.service;

import com.example.BeGroom.cart.domain.Cart;
import com.example.BeGroom.cart.domain.CartItem;
import com.example.BeGroom.cart.dto.CartItemResDto;
import com.example.BeGroom.cart.dto.CartResDto;
import com.example.BeGroom.cart.repository.CartItemRepository;
import com.example.BeGroom.cart.repository.CartRepository;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.domain.ProductImage;
import com.example.BeGroom.product.repository.ProductDetailRepository;
import com.example.BeGroom.product.repository.ProductImageRepository;
import com.example.BeGroom.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductImageRepository productImageRepository;

    // 장바구니 전체 조회 (DTO 변환)
    @Transactional
    public CartResDto getCart(Long memberId) {
        List<CartItem> cartItems = getCartItems(memberId);

        // CartItem -> CartItemResDto 변환
        List<CartItemResDto> itemDtos = cartItems.stream()
                .map(this::convertToDto)
                .toList();

        // 가격 자동 계산
        return CartResDto.from(itemDtos);
    }

    // 회원의 장바구니 조회 (관리자)
    @Transactional
    public Cart getOrCreateCart(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseGet(() -> createCart(memberId));
    }

    // 장바구니 생성
    @Transactional
    public Cart createCart(Long memberId) {
        Cart cart = Cart.builder()
                .memberId(memberId)
                .build();

        return cartRepository.save(cart);
    }

    // 장바구니 상품 목록 조회
    @Transactional
    public List<CartItem> getCartItems(Long memberId) {
        Cart cart = getOrCreateCart(memberId);
        return cartItemRepository.findByCartId(cart.getCartId());
    }

    // 선택된 상품 목록 조회
    @Transactional
    public List<CartItem> getSelectedItems(Long memberId) {
        Cart cart = getOrCreateCart(memberId);
        return cartItemRepository.findByCartIdAndIsSelected(cart.getCartId(), true);
    }

    // 장바구니에 상품 담기 (중복 상품은 수량 증가)
    @Transactional
    public CartItem addItem(Long memberId, Long productDetailId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        Cart cart = getOrCreateCart(memberId);

        // 중복 체크
        return cartItemRepository.findByCartIdAndProductDetailId(cart.getCartId(), productDetailId)
                .map(existingItem -> {
                    existingItem.increaseQuantity(quantity);
                    log.info("장바구니 상품 수량 증가 - cartItemId: {}, 기존: {}, 추가: {}",
                            existingItem.getCartItemId(),
                            existingItem.getQuantity() - quantity,
                            quantity);

                    return cartItemRepository.save(existingItem);
                })
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cartId(cart.getCartId())
                            .productDetailId(productDetailId)
                            .quantity(quantity)
                            .isSelected(true)
                            .build();

                    log.info("장바구니 상품 추가 - productDetailId: {}, quantity: {}",
                            productDetailId, quantity);

                    return cartItemRepository.save(newItem);
                });
    }

    // 상품 수량 변경 (+, - 조정이 아닌 숫자로 변경 시)
    @Transactional
    public CartItem updateQuantity(Long memberId, Long cartItemId, Integer quantity) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);

        cartItem.updateQuantity(quantity);
        log.info("장바구니 상품 수량 변경 - cartItemId: {}, quantity: {}", cartItemId, quantity);

        return cartItemRepository.save(cartItem);
    }

    // 상품 수량 증가
    @Transactional
    public CartItem increaseQuantity(Long memberId, Long cartItemId, Integer amount) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);

        cartItem.increaseQuantity(amount);
        log.info("장바구니 상품 수량 증가 - cartItemId: {}, amount: {}", cartItemId, amount);

        return cartItemRepository.save(cartItem);

    }

    // 상품 수량 감소
    @Transactional
    public CartItem decreaseQuantity(Long memberId, Long cartItemId, Integer amount) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);

        cartItem.decreaseQuantity(amount);
        log.info("장바구니 상품 수량 감소 - cartItemId: {}, amount: {}", cartItemId, amount);

        return cartItemRepository.save(cartItem);
    }

    // 상품 선택 상태 변경
    @Transactional
    public CartItem updateSelected(Long memberId, Long cartItemId, Boolean isSelected) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);

        cartItem.updateSelected(isSelected);
        log.info("장바구니 상품 선택 상태 변경 - cartItemId: {}, isSelected: {}",
                cartItemId, isSelected);

        return cartItemRepository.save(cartItem);
    }

    // 상품 전체 선택
    @Transactional
    public void selectAll(Long memberId) {
        Cart cart = getOrCreateCart(memberId);

        int updated = cartItemRepository.updateAllSelectedByCartId(cart.getCartId(), true);
        log.info("장바구니 전체 선택 - memberId: {}, updated: {}", memberId, updated);
    }

    // 상품 전체 선택 해제
    @Transactional
    public void deselectAll(Long memberId) {
        Cart cart = getOrCreateCart(memberId);

        int updated = cartItemRepository.updateAllSelectedByCartId(cart.getCartId(), false);
        log.info("장바구니 전체 선택 해제 - memberId: {}, updated: {}", memberId, updated);
    }

    // 상품 삭제 (개별)
    @Transactional
    public void deleteItem(Long memberId, Long cartItemId) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);

        cartItemRepository.delete(cartItem);
        log.info("장바구니 상품 삭제 - cartItemId: {}", cartItemId);
    }

    // 선택한 상품 삭제 (여러 개)
    @Transactional
    public void deleteSelectedItems(Long memberId) {
        Cart cart = getOrCreateCart(memberId);

        cartItemRepository.deleteByCartIdAndIsSelected(cart.getCartId(), true);
        log.info("장바구니 선택 상품 삭제 - memberId: {}", memberId);
    }

    // 장바구니 비우기 (전체 상품 삭제)
    @Transactional
    public void clearCart(Long memberId) {
        Cart cart = getOrCreateCart(memberId);

        cartItemRepository.deleteByCartId(cart.getCartId());
        log.info("장바구니 비우기 - memberId: {}", memberId);
    }

    // 장바구니 상품 개수 조회
    public long getItemCount(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId).orElse(null);
        if (cart == null) {
            return 0;
        }

        return cartItemRepository.countByCartId(cart.getCartId());
    }

    // 선택된 상품 개수 조회
    public long getSelectedItemCount(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId).orElse(null);
        if (cart == null) {
            return 0;
        }

        return cartItemRepository.countByCartIdAndIsSelected(cart.getCartId(), true);
    }


    // ===== Private 헬퍼 메서드 =====
    // CartItem -> CartItemDto 변환
    private CartItemResDto convertToDto(CartItem cartItem) {
        ProductDetail productDetail = productDetailRepository
                .findById(cartItem.getProductDetailId())
                .orElse(null);

        if (productDetail == null) {
            log.warn("ProductDetail을 찾을 수 없음 - productDetailId: {}", cartItem.getProductDetailId());

            return CartItemResDto.from(cartItem);
        }

        Product product = productRepository
                .findById(productDetail.getProductId())
                .orElse(null);

        if (product == null) {
            log.warn("Product를 찾을 수 없음 - productId: {}", productDetail.getProductId());

            return CartItemResDto.from(cartItem);
        }

        String mainImageUrl = productImageRepository
                .findByProductIdAndImageTypeOrderBySortOrderAsc(
                        product.getProductId(),
                        ProductImage.ImageType.MAIN
                )
                .stream()
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);

        return CartItemResDto.of(
                cartItem,
                product.getName(),
                mainImageUrl,
                productDetail.getName(),
                productDetail.getBasePrice(),
                productDetail.getDiscountedPrice(),
                !productDetail.getIsAvailable(),
                productDetail.getQuantity()
        );
    }

    // 장바구니 상품 조회 및 소유권 검증
    private CartItem getCartItemWithValidation(Long memberId, Long cartItemId) {
        Cart cart = getOrCreateCart(memberId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 상품을 찾을 수 없습니다."));

        // 소유권 검증
        if (!cartItem.getCartId().equals(cart.getCartId())) {
            throw new IllegalArgumentException("해당 장바구니 상품에 대한 권한이 없습니다.");
        }

        return cartItem;
    }

}