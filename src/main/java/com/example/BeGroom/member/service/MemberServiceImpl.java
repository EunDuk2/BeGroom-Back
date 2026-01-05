package com.example.BeGroom.member.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.*;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.repository.OrderProductRepository;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductImage;
import com.example.BeGroom.wallet.domain.Wallet;
import com.example.BeGroom.wallet.domain.WalletTransaction;
import com.example.BeGroom.wallet.repository.WalletRepository;
import com.example.BeGroom.wallet.repository.WalletTransactionRepository;
import com.example.BeGroom.wallet.service.WalletService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.stream.LangCollectors.collect;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final OrderRepository orderRepository;
    private final WalletTransactionRepository transactionRepository;

    @Override
    public Member create(MemberCreateReqDto reqDto) {
        // 존재 유무 검증
        if(memberRepository.findByEmail(reqDto.getEmail()).isPresent()) {
            throw new EntityExistsException("이미 존재하는 회원입니다.");
        }
        // 생성
        Member member = Member.createMember(
                reqDto.getEmail(),
                reqDto.getName(),
                passwordEncoder.encode(reqDto.getPassword()),
                reqDto.getPhoneNumber(),
                reqDto.getRole()
        );
        // 저장
        memberRepository.save(member);
        // 지갑 생성
        walletService.create(member);

        return member;
    }

    @Override
    public MemberGetProfileResDto getMyProfile(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        return MemberGetProfileResDto.from(member);
    }

    @Override
    public GetMemberOrdersResDto getMyOrders(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        List<Order> orders = orderRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);

        List<GetMemberOrdersResDto.OrderSummary> orderSummaries = orders.stream()
                .map(order -> {
                    List<GetMemberOrdersResDto.OrderedItem> items = order.getOrderProductList().stream()
                            .map(op -> {
                                Product product = op.getProduct();
                                String imageUrl = product.getImages().stream()
                                        .filter(image -> ProductImage.ImageType.MAIN.equals(image.getImageType()))
                                        .findFirst()
                                        .map(ProductImage::getImageUrl)
                                        .orElseGet(() -> !product.getImages().isEmpty()
                                                ? product.getImages().getFirst().getImageUrl()
                                                : "");

                                return GetMemberOrdersResDto.OrderedItem.builder()
                                        .imageUrl(imageUrl)
                                        .productName(product.getName())
                                        .price(op.getPrice())
                                        .quantity(op.getQuantity())
                                        .build();
                            })
                            .collect(Collectors.toList());
                    return GetMemberOrdersResDto.OrderSummary.builder()
                            .orderNumber(order.getId())
                            .totalAmount(order.getTotalAmount())
                            .status(order.getOrderStatus())
                            .createdAt(order.getCreatedAt())
                            .items(items)
                            .build();
                })
                .collect(Collectors.toList());

        return GetMemberOrdersResDto.of(orderSummaries);
    }

//    @Override
//    public GetMemberWishesResDto getMyWishes(Long memberId) {
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
//
//        return GetMemberWishesResDto.of(member);
//    }

    @Override
    public GetMemberWalletResDto getWalletTransactions(Long memberId, Pageable pageable) {
        Member memberProxy = memberRepository.getReferenceById(memberId);

        Wallet wallet = walletRepository.findByMember(memberProxy)
                .orElseThrow(() -> new EntityNotFoundException("지갑을 찾을 수 없습니다."));

        // 2. 지갑 ID로 거래 내역 페이징 조회 (쿼리 자동 실행)
        Page<WalletTransaction> transactionPage =
                transactionRepository.findByWalletOrderByCreatedAtDesc(wallet, pageable);

        // 3. Entity -> DTO 변환 (Page의 map 메서드 활용)
        Page<GetMemberWalletResDto.TransactionSummary> transactionDtoPage = transactionPage.map(tx ->
                GetMemberWalletResDto.TransactionSummary.builder()
                        .id(tx.getId())
                        .txType(tx.getTransactionType().toString())
                        .amount(tx.getAmount())
                        .balanceAfter(tx.getBalanceAfter())
                        .createdAt(tx.getCreatedAt())
                        .build()
        );

        GetMemberWalletResDto.WalletSummary walletSummary = GetMemberWalletResDto.WalletSummary.builder()
                .id(wallet.getId())
                .balance(wallet.getBalance())
                .build();

        // 5. 합쳐서 리턴 (이제 타입이 맞습니다!)
        return GetMemberWalletResDto.of(walletSummary, transactionDtoPage);
    }
}