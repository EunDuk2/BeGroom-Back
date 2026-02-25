package com.example.BeGroom.member.service;

import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.dto.*;
import com.example.BeGroom.member.repository.MemberJdbcRepository;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.domain.OrderProduct;
import com.example.BeGroom.order.repository.OrderProductRepository;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.product.domain.ImageType;
import com.example.BeGroom.product.domain.Product;
import com.example.BeGroom.product.domain.ProductDetail;
import com.example.BeGroom.product.domain.ProductImage;
import com.example.BeGroom.product.repository.ProductRepository;
import com.example.BeGroom.wallet.domain.Wallet;
import com.example.BeGroom.wallet.domain.WalletTransaction;
import com.example.BeGroom.wallet.repository.WalletRepository;
import com.example.BeGroom.wallet.repository.WalletTransactionRepository;
import com.example.BeGroom.wallet.service.WalletService;
import com.example.BeGroom.wishlist.domain.Wishlist;
import com.example.BeGroom.wishlist.repository.WishlistRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private final WishlistRepository wishlistRepository;
    private final MemberJdbcRepository memberJdbcRepository;
    private final ProductRepository productRepository;

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
    public void insertBulkMembers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int totalCount = 100_000;
        int batchSize = 10000;

        List<MemberBulkDto> batchList = new ArrayList<>(batchSize);

        for (int i = 1; i <= totalCount; i++) {
            String email = String.format("user%d@begroom.com", i);
            String name = "user" + i;

            int mid = 1000 + (i / 10000);
            int last = i % 10000;
            String phoneNumber = String.format("010-%04d-%04d", mid, last);

            batchList.add(new MemberBulkDto(email, name, phoneNumber));

            if (batchList.size() >= batchSize) {
                memberJdbcRepository.bulkInsert(batchList);
                batchList.clear();
            }
        }

        if (!batchList.isEmpty()) {
            memberJdbcRepository.bulkInsert(batchList);
        }

        stopWatch.stop();
        System.out.println("벌크 인서트 완료 시간: " + stopWatch.getDuration() + "초");
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
                                //
                                ProductDetail productDetail = op.getProductDetail();
                                Product product = productDetail.getProduct();
                                String imageUrl = product.getProductImages().stream()
                                        .filter(image -> ImageType.MAIN.equals(image.getImageType()))
                                        .findFirst()
                                        .map(ProductImage::getImageUrl)
                                        .orElseGet(() -> !product.getProductImages().isEmpty()
                                                ? product.getProductImages().getFirst().getImageUrl()
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

    @Override
    public GetMemberWishesResDto getMyWishes(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        List<Wishlist> wishlists = wishlistRepository.findAllByMember_Id(memberId);

        return GetMemberWishesResDto.from(wishlists);
    }

    @Override
    public GetMemberWalletResDto getWalletTransactions(Long memberId, Pageable pageable) {
        Member memberProxy = memberRepository.getReferenceById(memberId);

        Wallet wallet = walletRepository.findByMember(memberProxy)
                .orElseThrow(() -> new EntityNotFoundException("지갑을 찾을 수 없습니다."));

        Page<WalletTransaction> transactionPage =
                transactionRepository.findByWalletOrderByCreatedAtDesc(wallet, pageable);

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

        return GetMemberWalletResDto.of(walletSummary, transactionDtoPage);
    }
}