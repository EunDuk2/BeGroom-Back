package com.example.BeGroom.seed_order.service;

import com.example.BeGroom.common.security.JwtTokenProvider;
import com.example.BeGroom.member.domain.Member;
import com.example.BeGroom.member.domain.Role;
import com.example.BeGroom.member.repository.MemberRepository;
import com.example.BeGroom.order.domain.Order;
import com.example.BeGroom.order.repository.OrderProductRepository;
import com.example.BeGroom.order.repository.OrderRepository;
import com.example.BeGroom.order.service.OrderService;
import com.example.BeGroom.order.dto.OrderCreateReqDto;
import com.example.BeGroom.order.dto.OrderProductReqDto;
import com.example.BeGroom.payment.repository.PaymentRepository;
import com.example.BeGroom.product.domain.*;
import com.example.BeGroom.product.repository.*;
import com.example.BeGroom.seller.domain.Seller;
import com.example.BeGroom.seller.repository.SellerRepository;
import com.example.BeGroom.settlement.repository.SettlementRepository;
import com.example.BeGroom.wallet.domain.Wallet;
import com.example.BeGroom.seed_order.dto.*;
import com.example.BeGroom.wallet.repository.WalletRepository;
import com.example.BeGroom.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final SettlementRepository settlementRepository;

    private final SellerRepository sellerRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final StockRepository stockRepository;
    private final ProductPriceRepository productPriceRepository;

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    private final JwtTokenProvider jwtTokenProvider;

    // ---- seed config ----
    private String seedEmail = "seed@test.com";
    private String seedPassword = "1234";
    private String seedPhone = "010";
    private String seedName = "seed";

    private long seedWalletBalance = 1_000_000_000L;

    private boolean recreateCatalogEveryRun = false;

    private String sellerEmail = "seed-seller@test.com";
    private String sellerName = "seedSeller";
    private String sellerPassword = "1234";
    private String sellerPhone = "01012341234";

    private long brandCode = 999L;
    private String brandName = "seed-brand";

    private long productNo = 999L;
    private String productName = "seed-product";

    private long detailNo1 = 1L;
    private String detailName1 = "seed-detail-1";
    private int price1 = 3000;
    private int stock1 = 1_000_000;

    private long detailNo2 = 2L;
    private String detailName2 = "seed-detail-2";
    private int price2 = 5000;
    private int stock2 = 1_000_000;

    @Transactional
    public SeedRunResponse createRun(SeedRunRequest req) {
        String runId = (req.runId() == null || req.runId().isBlank())
                ? UUID.randomUUID().toString()
                : req.runId();

        int orderCount = Math.max(1, req.orderCount());

        // 1) member / wallet (재사용)
        Member member = memberRepository.findByEmail(seedEmail)
                .orElseGet(() -> memberRepository.save(
                        Member.createMember(seedEmail, seedName, seedPassword, seedPhone, Role.USER)
                ));
        // 토큰 생성
        String token = jwtTokenProvider.createToken(member.getId(), member.getEmail(), "USER");

        walletRepository.findByMember(member)
                .orElseGet(() -> walletRepository.save(Wallet.create(member, seedWalletBalance)));

        // 2) seller/brand/product (재사용 or run마다 새로)
        Catalog catalog = ensureCatalog(runId);

        // 3) run 전용 ProductDetail 2개 생성 (Stock 자동 생성)
        ProductDetail pd1 = createRunProductDetail(catalog.product, runId, detailNo1, detailName1, price1, stock1);
        ProductDetail pd2 = createRunProductDetail(catalog.product, runId, detailNo2, detailName2, price2, stock2);

        // 4) 주문 N개 생성 (checkout만 할 거라 status=CREATED 상태로 남겨둠)
        OrderCreateReqDto orderReq = new OrderCreateReqDto(List.of(
                new OrderProductReqDto(pd1.getId(), 1),
                new OrderProductReqDto(pd2.getId(), 2)
        ));

        List<Long> orderIds = new ArrayList<>(orderCount);
        for (int i = 0; i < orderCount; i++) {
            Order order = orderService.create(member.getId(), orderReq);
            orderIds.add(order.getId());
        }

        return new SeedRunResponse(
                runId,
                member.getId(),
                token,
                orderIds,
                List.of(pd1.getId(), pd2.getId())
        );
    }

    @Transactional
    public String createRunAsCsv(SeedRunRequest req) {
        SeedRunResponse res = createRun(req);

        // 1행: memberId
        // 2행부터: orderId
        StringBuilder sb = new StringBuilder();
        sb.append("memberId\n");
        sb.append(res.memberId()).append("\n");
        sb.append("token\n");
        sb.append(res.token()).append("\n");
        sb.append("\n");
        sb.append("orderId\n");
        for (Long id : res.orderIds()) {
            sb.append(id).append("\n");
        }
        sb.append("productDetailIds\n");
        for (Long id : res.productDetailIds()) {
            sb.append(id).append("\n");
        }
        return sb.toString();
    }

    /**
     * run 정리:
     * - runId로 만들어진 productDetail을 찾아서
     * - 그 productDetail을 참조하는 order를 찾아서 삭제
     * - orderProduct 삭제
     * - order 삭제
     * - stock/productPrice/productDetail 삭제(필요한 순서대로)
     *
     * ⚠️ 프로젝트 FK 설정/연관관계 cascade에 따라 순서 조정 필요할 수 있음
     */
    @Transactional
    public void cleanupRun() {
        settlementRepository.deleteAllInBatch();

        // 1) Payment (order FK 또는 payment->order FK 있으면 최우선)
        paymentRepository.deleteAllInBatch();

        // 2) OrderProduct (order FK)
        orderProductRepository.deleteAllInBatch();

        // 3) Order (member FK 등)
        orderRepository.deleteAllInBatch();

        // 4) WalletTransaction (wallet FK)
        walletTransactionRepository.deleteAllInBatch();

        // 5) Stock / Price (product_detail FK)
        stockRepository.deleteAllInBatch();
        productPriceRepository.deleteAllInBatch();

        // 6) ProductDetail
        productDetailRepository.deleteAllInBatch();
    }

    // --------------------------
    // internal helpers
    // --------------------------

    private Catalog ensureCatalog(String runId) {
        if (recreateCatalogEveryRun) {
            // run 단위로 완전 격리(데이터 늘어남)
            Seller seller = sellerRepository.save(
                    Seller.createSeller("run-" + runId + "-" + sellerEmail, sellerName, sellerPassword, sellerPhone)
            );
            Brand brand = brandRepository.save(
                    Brand.builder().seller(seller).brandCode(brandCode).name(brandName).build()
            );
            Product product = productRepository.save(
                    Product.builder().brand(brand).no(productNo).name(productName).productStatus(ProductStatus.SALE).build()
            );
            return new Catalog(seller, brand, product);
        }

        // 재사용 (없으면 생성)
        Seller seller = sellerRepository.findByEmail(sellerEmail)
                .orElseGet(() -> sellerRepository.save(
                        Seller.createSeller(sellerEmail, sellerName, sellerPassword, sellerPhone)
                ));

        Brand brand = brandRepository.findBySellerAndBrandCode(seller, brandCode)
                .orElseGet(() -> brandRepository.save(
                        Brand.builder().seller(seller).brandCode(brandCode).name(brandName).build()
                ));

        Product product = productRepository.findByBrandAndNo(brand, productNo)
                .orElseGet(() -> productRepository.save(
                        Product.builder().brand(brand).no(productNo).name(productName).productStatus(ProductStatus.SALE).build()
                ));

        return new Catalog(seller, brand, product);
    }

    private ProductDetail createRunProductDetail(
            Product product,
            String runId,
            long baseNo,
            String baseName,
            int price,
            int quantity
    ) {
        // run마다 격리: no를 run 기반으로 바꾸거나 name prefix로 격리
        // no가 unique면 충돌 나니까 runHash 섞어서 고유하게 만들기
        long runSuffix = Math.abs(runId.hashCode() % 1_000_000);
        long no = baseNo * 1_000_000 + runSuffix;

        ProductDetail pd = ProductDetail.builder()
                .product(product)
                .no(no)
                .name("run-" + runId + "-" + baseName)
                .initialQuantity(quantity)
                .build();

        pd.addPrice(price, price);
        // Stock 자동 생성이 ProductDetail 내부에서 된다면 여기서 따로 호출 필요 없음
        return productDetailRepository.save(pd);
    }

    private record Catalog(Seller seller, Brand brand, Product product) {}
}

