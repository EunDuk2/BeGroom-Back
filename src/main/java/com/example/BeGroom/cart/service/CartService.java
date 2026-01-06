package com.example.BeGroom.cart.service;

import com.example.BeGroom.cart.domain.Cart;
import com.example.BeGroom.cart.domain.CartItem;
import com.example.BeGroom.cart.dto.CartItemResDto;
import com.example.BeGroom.cart.dto.CartResDto;
import com.example.BeGroom.cart.repository.CartItemRepository;
import com.example.BeGroom.cart.repository.CartRepository;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.domain.ProductImage;
import com.example.BeGroom.product.domain.ProductOption;
import com.example.BeGroom.product.repository.ProductDetailRepository;
import com.example.BeGroom.product.repository.ProductImageRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final MemberRepository memberRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductImageRepository productImageRepository;

    // 장바구니 전체 조회 (DTO 변환)
    @Transactional
    public CartResDto getCart(Long memberId) {
        Cart cart = getOrCreateCart(memberId);

        List<CartItem> sortedItems = cartItemRepository.findByCart_CartIdOrderByCreatedAtDesc(cart.getCartId());

        // CartItem -> CartItemResDto 변환
        List<CartItemResDto> itemDto = sortedItems.stream()
                .map(this::convertToDto)
                .toList();

        // 가격 자동 계산
        return CartResDto.from(itemDto);
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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Cart cart = Cart.create(member);
        return cartRepository.save(cart);
    }

    // 장바구니에 상품 담기 (중복 상품은 수량 증가)
    @Transactional
    public void addItem(Long memberId, Long productDetailId, Integer quantity) {
        Cart cart = getOrCreateCart(memberId);
        ProductDetail productDetail = productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new EntityNotFoundException("상품 옵션 정보를 찾을 수 없습니다."));

        cartItemRepository.findByCart_CartIdAndProductDetail_ProductDetailId(cart.getCartId(), productDetailId)
                .ifPresentOrElse(
                        existingItem -> {
                            existingItem.increaseQuantity(quantity);
                            log.info("기존 상품 수량 증가: cartItemId={}, quantity={}", existingItem.getCartItemId(), existingItem.getQuantity());
                        },
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .cart(cart)
                                    .productDetail(productDetail)
                                    .quantity(quantity)
                                    .isSelected(true)
                                    .build();

                            newItem.updateQuantity(quantity);
                            cart.addItem(newItem);
                            cartItemRepository.save(newItem);
                            log.info("새로운 상품 추가: productDetailId={}, quantity={}", productDetailId, quantity);
                        }
                );

    }

    // 상품 수량 변경 (+, - 조정이 아닌 숫자로 변경 시)
    @Transactional
    public void updateQuantity(Long memberId, Long cartItemId, Integer quantity) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);
        cartItem.updateQuantity(quantity);
        log.info("장바구니 상품 수량 변경 - cartItemId: {}, quantity: {}", cartItemId, quantity);
    }

    // 상품 수량 증가
    @Transactional
    public void increaseQuantity(Long memberId, Long cartItemId, Integer amount) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);
        cartItem.increaseQuantity(amount);
        log.info("장바구니 상품 수량 증가 - cartItemId: {}, amount: {}", cartItemId, amount);
    }

    // 상품 수량 감소
    @Transactional
    public void decreaseQuantity(Long memberId, Long cartItemId, Integer amount) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);
        cartItem.decreaseQuantity(amount);
        log.info("장바구니 상품 수량 감소 - cartItemId: {}, amount: {}", cartItemId, amount);
    }

    // 상품 선택 상태 변경
    @Transactional
    public void updateSelected(Long memberId, Long cartItemId, Boolean isSelected) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);
        cartItem.updateSelected(isSelected);
        log.info("장바구니 상품 선택 상태 변경 - cartItemId: {}, isSelected: {}",
                cartItemId, isSelected);
    }

    // 상품 전체 선택 / 선택 해제
    @Transactional
    public void updateAllSelection(Long memberId, Boolean isSelected) {
        Cart cart = getOrCreateCart(memberId);
        cartItemRepository.updateAllSelectedByCartId(cart.getCartId(), isSelected);
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

        cartItemRepository.deleteByCart_CartIdAndIsSelected(cart.getCartId(), true);
        log.info("장바구니 선택 상품 삭제 - memberId: {}", memberId);
    }

    // 장바구니 비우기 (전체 상품 삭제)
    @Transactional
    public void clearCart(Long memberId) {
        Cart cart = getOrCreateCart(memberId);

        cartItemRepository.deleteByCart_CartId(cart.getCartId());
        log.info("장바구니 비우기 - memberId: {}", memberId);
    }

    // 장바구니 상품 개수 조회
    public long getItemCount(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .map(cart -> cartItemRepository.countByCart_CartId(cart.getCartId()))
                .orElse(0L);
    }

    // ===== Private 헬퍼 메서드 =====
    // CartItem -> CartItemDto 변환
    private CartItemResDto convertToDto(CartItem cartItem) {
        ProductDetail productDetail = cartItem.getProductDetail();
        Product product = productDetail.getProduct();

        String deliveryType = productDetail.getOptionMappings().stream()
                .map(mapping -> mapping.getProductOption())
                .filter(option -> "delivery".equals(option.getOptionType()))
                .map(ProductOption::getOptionValue)
                .findFirst()
                .orElse("NORMAL_PARCEL");

        String mainImageUrl = productImageRepository
                .findByProduct_ProductIdAndImageTypeOrderBySortOrderAsc(
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
                productDetail.getQuantity(),
                deliveryType
        );
    }

    // 장바구니 상품 조회 및 소유권 검증
    private CartItem getCartItemWithValidation(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 상품을 찾을 수 없습니다."));

        // 소유권 검증
        if (!cartItem.getCart().getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("해당 장바구니 상품에 대한 권한이 없습니다.");
        }

        return cartItem;
    }

}