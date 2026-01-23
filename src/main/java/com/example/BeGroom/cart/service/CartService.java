package com.example.BeGroom.cart.service;

import com.example.BeGroom.cart.domain.Cart;
import com.example.BeGroom.cart.domain.CartItem;
import com.example.BeGroom.cart.dto.CartItemResponse;
import com.example.BeGroom.cart.dto.CartRequest;
import com.example.BeGroom.cart.dto.CartResponse;
import com.example.BeGroom.cart.repository.CartItemRepository;
import com.example.BeGroom.cart.repository.CartRepository;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.repository.ProductDetailRepository;
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

    // 장바구니 전체 조회
    @Transactional
    public CartResponse getCart(Long memberId) {
        Cart cart = getOrCreateCart(memberId);

        List<CartItemResponse> itemDtos = cart.getCartItems().stream()
                .map(CartItemResponse::from)
                .toList();

        return CartResponse.from(itemDtos);
    }

    // 장바구니에 상품 담기 (중복 상품은 수량 증가)
    @Transactional
    public void addItems(Long memberId, List<CartRequest.CartItemAdd> itemRequests) {
        Cart cart = getOrCreateCart(memberId);

        for (CartRequest.CartItemAdd req : itemRequests) {
            ProductDetail productDetail = productDetailRepository.findById(req.getProductDetailId())
                .orElseThrow(() -> new EntityNotFoundException("상품 정보를 찾을 수 없습니다."));

            cart.addProduct(productDetail, req.getQuantity());
        }

        cartRepository.save(cart);
    }

    // 상품 수량 변경 (+, - 조정이 아닌 숫자로 변경 시)
    @Transactional
    public void updateQuantity(Long memberId, Long cartItemId, int quantity) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);
        cartItem.updateQuantity(quantity);
        log.info("장바구니 상품 수량 변경 - cartItemId: {}, quantity: {}", cartItemId, quantity);
    }

    // 상품 수량 증가
    @Transactional
    public void increaseQuantity(Long memberId, Long cartItemId, int amount) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);
        cartItem.increaseQuantity(amount);
        log.info("장바구니 상품 수량 증가 - cartItemId: {}, amount: {}", cartItemId, amount);
    }

    // 상품 수량 감소
    @Transactional
    public void decreaseQuantity(Long memberId, Long cartItemId, int amount) {
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
        cart.getCartItems().forEach(item -> item.updateSelected(isSelected));
    }

    // 개별 상품 삭제
    @Transactional
    public void deleteItem(Long memberId, Long cartItemId) {
        CartItem cartItem = getCartItemWithValidation(memberId, cartItemId);
        cartItem.getCart().getCartItems().remove(cartItem);
        log.info("장바구니 상품 삭제 - cartItemId: {}", cartItemId);
    }

    // 선택한 상품 삭제 (여러 개)
    @Transactional
    public void deleteSelectedItems(Long memberId) {
        Cart cart = getOrCreateCart(memberId);
        cart.getCartItems().removeIf(CartItem::getIsSelected);
    }

    // 장바구니 비우기 (전체 상품 삭제)
    @Transactional
    public void clearCart(Long memberId) {
        Cart cart = getOrCreateCart(memberId);
        cart.getCartItems().clear();
    }

    // 장바구니 상품 개수 조회
    public long getItemCount(Long memberId) {
        return cartRepository.findByMemberId(memberId)
            .map(cart -> (long) cart.getCartItems().size())
            .orElse(0L);
    }


    // 회원의 장바구니 조회 (관리자)
    @Transactional
    public Cart getOrCreateCart(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

                    Cart newCart = Cart.create(member);
                    return cartRepository.save(newCart);
                });
    }

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